#include <iostream>
#include <windows.h>
#include <wininet.h>
#include <string>
#include <vector>
#include <memory>
#include <cstdio>

#pragma comment(lib, "wininet.lib") // 引入 WinINet 库
std::string ConvertLPWSTRToString(const std::wstring& wideStr) {
    int size_needed = WideCharToMultiByte(CP_UTF8, 0, wideStr.c_str(), -1, nullptr, 0, nullptr, nullptr);
    std::string str(size_needed, 0);
    WideCharToMultiByte(CP_UTF8, 0, wideStr.c_str(), -1, &str[0], size_needed, nullptr, nullptr);
    return str;
}
bool isJavaInstalledA() {
    std::string command = "java -version";
    std::string result;

    // 通过系统命令获取 Java 版本
    std::vector<char> buffer;
    std::shared_ptr<FILE> pipe(_popen(command.c_str(), "r"), _pclose);
    if (!pipe) {
        std::cerr << "无法运行 Java 版本检查命令" << std::endl;
        return false;
    }

    while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
        result += buffer.data();
    }

    return system("java -version") == 0;
}
bool isJavaInstalledB() {
    std::string command = ".\\jdk\\jdk-21.0.5+11\\bin\\java.exe -version";
    std::string result;

    // 通过系统命令获取 Java 版本
    std::vector<char> buffer;
    std::shared_ptr<FILE> pipe(_popen(command.c_str(), "r"), _pclose);
    if (!pipe) {
        std::cerr << "无法运行 Java 版本检查命令" << std::endl;
        return false;
    }

    while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
        result += buffer.data();
    }
    return system(".\\jdk\\jdk-21.0.5+11\\bin\\java.exe -version") == 0;
}
bool isJavaInstalled() {
    return(isJavaInstalledA() or isJavaInstalledB());
}
// 启动指定的 Java 文件并传递其他参数
void runJavaFile(const std::vector<std::string>& args) {
    if (args.empty()) {
        std::cerr << "请提供 Java 文件作为第一个参数" << std::endl;
        return;
    }

    std::string javaFile = args[0]; // 第一个参数是 Java 文件名
    std::string command;
    if (isJavaInstalledA()) {
        command = "java -jar " + javaFile;
    }
    if (isJavaInstalledB()) {
        command = ".\\jdk\\jdk-21.0.5+11\\bin\\java.exe -jar " + javaFile;
    }
    if (!isJavaInstalled()) {
        return;
    }
    // 将其他参数添加到命令中
    for (size_t i = 1; i < args.size(); ++i) {
        command += " " + args[i];
    }

    std::cout << "运行命令: " << command << std::endl;

    // 执行命令
    int result = system(command.c_str());
    if (result == 0) {
        std::cout << "Java 文件运行成功!" << std::endl;
    }
    else {
        std::cerr << "Java 文件运行失败!" << std::endl;
    }
}
// 获取文件的大小
DWORD getFileSize(const std::string& url) {
    HINTERNET hInternet, hConnect;
    DWORD fileSize = 0;
    DWORD len = sizeof(fileSize);

    // 初始化 WinINet
    hInternet = InternetOpen(L"JavaInstaller", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
    if (hInternet == NULL) {
        std::cerr << "无法初始化 WinINet!" << std::endl;
        return 0;
    }

    // 连接到 URL
    hConnect = InternetOpenUrlA(hInternet, url.c_str(), NULL, 0, INTERNET_FLAG_RELOAD, 0);
    if (hConnect == NULL) {
        std::cerr << "无法连接到 URL: " << url << std::endl;
        InternetCloseHandle(hInternet);
        return 0;
    }

    // 获取文件大小
    if (HttpQueryInfoA(hConnect, HTTP_QUERY_CONTENT_LENGTH | HTTP_QUERY_FLAG_NUMBER, &fileSize, &len, NULL)) {
        InternetCloseHandle(hConnect);
        InternetCloseHandle(hInternet);
        return fileSize;
    }

    InternetCloseHandle(hConnect);
    InternetCloseHandle(hInternet);
    return 0;
}
bool close = false;
bool IsRunningAsAdmin() {
    BOOL isAdmin = FALSE;
    PSID administratorsGroup = NULL;

    // 获取管理员组 SID
    SID_IDENTIFIER_AUTHORITY NtAuthority = SECURITY_NT_AUTHORITY;
    if (AllocateAndInitializeSid(&NtAuthority, 2, SECURITY_BUILTIN_DOMAIN_RID, DOMAIN_ALIAS_RID_ADMINS,
        0, 0, 0, 0, 0, 0, &administratorsGroup)) {
        // 检查当前进程是否属于管理员组
        CheckTokenMembership(NULL, administratorsGroup, &isAdmin);
        FreeSid(administratorsGroup);
    }

    return isAdmin == TRUE;
}
void RequestUACPrivilege() {
    if (IsRunningAsAdmin()) {
        return;
    }
    // 获取当前可执行文件的路径
    WCHAR szFilePath[MAX_PATH];
    GetModuleFileName(NULL, szFilePath, MAX_PATH);

    // 使用 ShellExecute 启动自己并请求管理员权限
    SHELLEXECUTEINFO sei = { 0 };
    sei.cbSize = sizeof(sei);
    sei.fMask = SEE_MASK_DEFAULT;
    sei.hwnd = NULL;
    sei.lpVerb = L"runas"; // 以管理员身份运行
    sei.lpFile = szFilePath; // 当前程序路径
    sei.nShow = SW_NORMAL;

    if (!ShellExecuteEx(&sei)) {
        std::cerr << "请求 UAC 提升失败!" << std::endl;
    }
    else {
        std::cout << "请求 UAC 提升成功!" << std::endl;
    }
    close = true;
}

void setPermanentJavaEnvironmentVariable(const std::string& javaHome) {
    HKEY hKey;
    long result;
    std::string javaHomeValue = javaHome;
    std::string pathValue = javaHome + "\\bin";  // Java 的 bin 目录

    // 打开系统环境变量的注册表项
    result = RegOpenKeyEx(HKEY_LOCAL_MACHINE, L"System\\CurrentControlSet\\Control\\Session Manager\\Environment", 0, KEY_SET_VALUE, &hKey);
    if (result != ERROR_SUCCESS) {
        std::cerr << "无法打开注册表项 HKEY_LOCAL_MACHINE\\System\\CurrentControlSet\\Control\\Session Manager\\Environment!" << std::endl;
        return;
    }

    // 设置 JAVA_HOME 环境变量
    result = RegSetValueExW(hKey, L"JAVA_HOME", 0, REG_SZ, (const BYTE*)javaHomeValue.c_str(), javaHomeValue.size() + 1);
    if (result == ERROR_SUCCESS) {
        std::cout << "JAVA_HOME 环境变量设置成功!" << std::endl;
    }
    else {
        std::cerr << "设置 JAVA_HOME 环境变量失败! 错误代码: " << GetLastError() << std::endl;
    }

    // 获取当前的 PATH 环境变量
    DWORD pathLength = GetEnvironmentVariable(L"PATH", nullptr, 0);
    if (pathLength == 0) {
        std::cerr << "无法获取当前 PATH 环境变量!" << std::endl;
        RegCloseKey(hKey);
        return;
    }

    std::wstring currentPath(pathLength, L'\0');
    GetEnvironmentVariable(L"PATH", &currentPath[0], pathLength);

    // 更新 PATH 环境变量，追加 Java bin 目录
    std::string currentPathStr = ConvertLPWSTRToString(currentPath);

    // 仅在当前 PATH 环境变量中没有 Java 路径时追加
    if (currentPathStr.find(pathValue) == std::string::npos) {
        std::string newPath = currentPathStr + ";" + pathValue;

        result = RegSetValueExW(hKey, L"Path", 0, REG_SZ, (const BYTE*)newPath.c_str(), newPath.size() + 1);
        if (result == ERROR_SUCCESS) {
            std::cout << "PATH 环境变量更新成功!" << std::endl;
        }
        else {
            std::cerr << "更新 PATH 环境变量失败! 错误代码: " << GetLastError() << std::endl;
        }
    }
    else {
        std::cout << "Java bin 目录已经在 PATH 环境变量中，无需更新。" << std::endl;
    }

    // 关闭注册表键
    RegCloseKey(hKey);
}
// 下载文件并显示进度
bool downloadFile(const std::string& url, const std::string& dest) {
    HINTERNET hInternet, hConnect;
    DWORD bytesRead, totalBytesRead = 0, fileSize = getFileSize(url);
    char buffer[4096];
    BOOL bResults = FALSE;

    if (fileSize == 0) {
        std::cerr << "无法获取文件大小，下载失败。" << std::endl;
        return false;
    }

    // 初始化 WinINet
    hInternet = InternetOpen(L"JavaInstaller", INTERNET_OPEN_TYPE_DIRECT, NULL, NULL, 0);
    if (hInternet == NULL) {
        std::cerr << "无法初始化 WinINet!" << std::endl;
        return false;
    }

    // 连接到服务器
    hConnect = InternetOpenUrlA(hInternet, url.c_str(), NULL, 0, INTERNET_FLAG_RELOAD, 0);
    if (hConnect == NULL) {
        std::cerr << "无法连接到 URL: " << url << std::endl;
        InternetCloseHandle(hInternet);
        return false;
    }

    // 打开目标文件
    HANDLE hFile = CreateFileA(dest.c_str(), GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    if (hFile == INVALID_HANDLE_VALUE) {
        std::cerr << "无法打开文件 " << dest << std::endl;
        InternetCloseHandle(hConnect);
        InternetCloseHandle(hInternet);
        return false;
    }

    std::cout << "下载进度: 0%";

    // 循环读取并写入文件
    while ((bResults = InternetReadFile(hConnect, buffer, sizeof(buffer), &bytesRead)) && bytesRead > 0) {
        DWORD bytesWritten;
        WriteFile(hFile, buffer, bytesRead, &bytesWritten, NULL);
        totalBytesRead += bytesWritten;

        // 计算下载进度
        int progress = (totalBytesRead * 100) / fileSize;
        std::cout << "\r下载进度: " << progress << "%";
        std::cout.flush();
    }

    std::cout << "\n下载完成！" << std::endl;

    // 关闭文件和连接
    CloseHandle(hFile);
    InternetCloseHandle(hConnect);
    InternetCloseHandle(hInternet);

    return true;
}
// 安装 Java
void installJava(const std::string& dest) {
    if (isJavaInstalled()) {
        std::cout << "系统中已安装 Java！" << std::endl;
        return;
    }

    std::cout << "开始解压 Java..." << std::endl;
        std::string extractCommand = "powershell -Command \"Expand-Archive -Path '" + dest + "' -DestinationPath '.\\jdk\\'\"";
        if (system(extractCommand.c_str()) == 0) {
            std::cout << "安装成功！" << std::endl;
        }
        else {
            std::cerr << "安装失败！" << std::endl;
        }

}
int main(int argc, char* argv[]) {
    if (argc > 1) {
        std::cout << argv[1] << std::endl;
        if (std::string(argv[1]) == "-version") {
            return -1;
        }
    }
    std::string url = "https://aka.ms/download-jdk/microsoft-jdk-21.0.5-windows-x64.zip";
    std::string dest = "microsoft-jdk-21.0.5-windows-x64.zip"; // 下载的文件保存路径
    if (isJavaInstalled() == false) {
        std::cerr << "Java 未安装！现在为你安装microsoft-jdk-21.0.5 （JAVA21）" << std::endl;
        RequestUACPrivilege();
        if (close) return 0;

        if (downloadFile(url, dest)) {
            std::cout << "文件下载成功，保存到 " << dest << std::endl;
        }
        else {
            std::cerr << "文件下载失败。" << std::endl;
            std::cerr << "Java 安装失败！" << std::endl;
            return 1;
        }
        installJava(dest);

        // 检查是否安装 Java
        if (!isJavaInstalled()) {
            std::cerr << "Java 安装失败！" << std::endl;
            system("pause");
            return 1;
        }
    }
    else {
        if (argc > 1) {
            std::vector<std::string> args(argv + 1, argv + argc); // 将命令行参数转为 vector
            runJavaFile(args);
        }
        else {
            std::cerr << "没有提供 Java 文件的参数" << std::endl;
        }
    }


    system("pause");
    return 0;
}

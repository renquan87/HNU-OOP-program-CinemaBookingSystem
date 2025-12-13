import os

# ================= 配置区域 =================
# 要扫描的根目录（'.' 表示当前目录）
source_dir = '.'
# 输出文件
output_file = 'frontend_code.txt'

# ✅ 需要抓取的文件后缀 (Vue项目核心是这些)
valid_extensions = ['.vue', '.ts', '.js', '.tsx', '.json']

# ❌ 必须强制忽略的文件夹 (防止卡死)
ignored_dirs = {'node_modules', 'dist', 'public', '.git', '.vscode', '.idea', 'mock'}

# ❌ 必须忽略的具体文件名 (太长且无用)
ignored_files = {'pnpm-lock.yaml', 'yarn.lock', 'package-lock.json', 'stats.html'}
# ===========================================

def merge_frontend_files():
    print(f"🚀 开始扫描 Vue 项目... (已自动屏蔽 node_modules)")

    with open(output_file, 'w', encoding='utf-8') as outfile:
        file_count = 0

        for root, dirs, files in os.walk(source_dir):
            # 1. 智能修改 dirs 列表，从源头阻止扫描 node_modules
            # (这一步非常关键，能极大提高速度)
            dirs[:] = [d for d in dirs if d not in ignored_dirs]

            for file in files:
                # 检查文件名是否在黑名单
                if file in ignored_files:
                    continue

                # 检查后缀名
                _, ext = os.path.splitext(file)
                if ext in valid_extensions:
                    file_path = os.path.join(root, file)
                    file_count += 1

                    # 写入分隔符
                    outfile.write(f"\n\n{'='*20} File: {file_path} {'='*20}\n")

                    try:
                        with open(file_path, 'r', encoding='utf-8') as infile:
                            outfile.write(infile.read())
                    except Exception as e:
                        outfile.write(f"// [Error reading file]: {e}\n")

    print(f"✅ 搞定！已合并 {file_count} 个文件到 {output_file}")
    print(f"📄 请把 {output_file} 发送给 AI")

if __name__ == '__main__':
    merge_frontend_files()

import http.server
import socketserver
import socket
import os
import sys

# 解决 Windows 控制台编码问题
if sys.platform.startswith("win"):
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

PORT = 8080

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "192.168.5.9"

local_ip = get_local_ip()

class NoCacheHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=os.path.dirname(os.path.abspath(__file__)), **kwargs)

    def end_headers(self):
        # 强制禁用一切缓存，确保手机每次下拉刷新都能立即拿到最新代码
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        super().end_headers()

    def log_message(self, format, *args):
        try:
            sys.stdout.write(f"[手机访问] {args[0]} - {args[1]}\n")
            sys.stdout.flush()
        except Exception:
            pass

try:
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("0.0.0.0", PORT), NoCacheHandler) as httpd:
        print("\n" + "="*56)
        print("  [XiuXiu Photo Clean] 手机真机体验服务 (禁用缓存版) 已启动！")
        print("="*56)
        print(f"\n>> 请确保手机连接在同一个 WiFi 下")
        print(f"\n>> 访问链接：http://{local_ip}:{PORT}/?t=new")
        print("="*56 + "\n")
        sys.stdout.flush()
        httpd.serve_forever()
except Exception as e:
    print(f"启动异常: {e}")

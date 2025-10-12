@echo off
echo 启动文章服务调试模式...
echo 日志将输出到 debug.log 文件...
cd /d %~dp0
java -jar target/article-service-1.0-SNAPSHOT.jar --debug > debug.log 2>&1
echo 启动完成，查看 debug.log 获取详细信息
pause
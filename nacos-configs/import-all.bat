@echo off
echo 正在导入配置文件到Nacos...
echo.

set NACOS_URL=http://localhost:8848
set GROUP=DEFAULT_GROUP

rem 导入共享配置
echo 导入共享MySQL配置...
curl -X POST "%NACOS_URL%/nacos/v1/cs/configs" -H "Content-Type: application/x-www-form-urlencoded" -d "dataId=shared-mysql-dev.yml&group=%GROUP%&content=@shared-mysql-dev.yml&type=yaml"

echo 导入共享Redis配置...
curl -X POST "%NACOS_URL%/nacos/v1/cs/configs" -H "Content-Type: application/x-www-form-urlencoded" -d "dataId=shared-redis-dev.yml&group=%GROUP%&content=@shared-redis-dev.yml&type=yaml"

rem 导入服务配置
echo 导入article-service配置...
curl -X POST "%NACOS_URL%/nacos/v1/cs/configs" -H "Content-Type: application/x-www-form-urlencoded" -d "dataId=article-service-dev.yml&group=%GROUP%&content=@article-service-dev.yml&type=yaml"

echo 导入ai-service配置...
curl -X POST "%NACOS_URL%/nacos/v1/cs/configs" -H "Content-Type: application/x-www-form-urlencoded" -d "dataId=ai-service-dev.yml&group=%GROUP%&content=@ai-service-dev.yml&type=yaml"

echo 导入gateway配置...
curl -X POST "%NACOS_URL%/nacos/v1/cs/configs" -H "Content-Type: application/x-www-form-urlencoded" -d "dataId=gateway-dev.yml&group=%GROUP%&content=@gateway-dev.yml&type=yaml"

echo 导入user-service配置...
curl -X POST "%NACOS_URL%/nacos/v1/cs/configs" -H "Content-Type: application/x-www-form-urlencoded" -d "dataId=user-service-dev.yml&group=%GROUP%&content=@user-service-dev.yml&type=yaml"

echo 导入report-service配置...
curl -X POST "%NACOS_URL%/nacos/v1/cs/configs" -H "Content-Type: application/x-www-form-urlencoded" -d "dataId=report-service-dev.yml&group=%GROUP%&content=@report-service-dev.yml&type=yaml"

echo.
echo 所有配置导入完成！
echo 请检查Nacos控制台确认配置已生效
echo 访问: http://localhost:8848/nacos
pause
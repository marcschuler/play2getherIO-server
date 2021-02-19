docker build -t play2gether.io-server .
docker run -d -p 3101:3101 --name play2gether.io-server play2gether.io-server

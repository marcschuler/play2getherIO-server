docker build -t play2gether.io-server .
docker run -d -p 3101:3101 --env-file=../p2g-server.secrets --name play2gether.io-server play2gether.io-server

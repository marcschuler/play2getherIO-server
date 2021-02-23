docker build --build-arg "$(cat ../p2g-server.secrets.env)" -t play2gether.io-server .
docker run --env-file ../p2g-server.secrets.env -d -p 3101:3101 --name play2gether.io-server play2gether.io-server

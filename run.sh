#!/bin/sh

PORT=${PORT:-8080}

root="$(cd "$(dirname "$0")" && pwd)"

if ! [ -r "$root/db-config.json" ]; then
    echo "Please specify a db-config.json. Something like this will do:" 1>&2
    echo "cat <<'EOF' > db-config.json" 1>&2
    echo "{" 1>&2
    echo '    "endpoint": "mysql://distelli-alpha.cedf018eguc7.us-east-1.rds.amazonaws.com:3306/addr_'"$USER"'",' 1>&2
    echo '    "user": "distelli",' 1>&2
    echo '    "password": "<obtain from 1Password/Puppet/Pipelines/Mysql: distelli-alpha>"' 1>&2
    echo "}" 1>&2
    echo "EOF" 1>&2
    exit 1
fi

exec java -cp "$root/target/classes:$(cat "$root/target/.classpath")" \
     "-Dlog4j.configuration=file://$root/log4j.properties" \
     run.Guice \
     -Mcom.puppet.pipelines.api.addr.AddressBookAPIModule \
     -Mcom.distelli.persistence.impl.PersistenceModule="$root/db-config.json" \
     com.distelli.webserver.GuiceWebServer.run=$PORT

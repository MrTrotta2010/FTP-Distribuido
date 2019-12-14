javac connectionserver/*.java
gnome-terminal -- /bin/bash -c 'rmiregistry; read'
gnome-terminal -- /bin/bash -c 'java -Djava.rmi.server.codebase=file:. -Djava.rmi.server.hostname="172.18.0.206" connectionserver/FileServer; read'
gnome-terminal -- /bin/bash -c 'java -classpath ".:sqlite-jdbc-3.27.2.1.jar" -Djava.rmi.server.codebase=file:. -Djava.rmi.server.hostname="172.18.0.206" connectionserver/UserServer; read'
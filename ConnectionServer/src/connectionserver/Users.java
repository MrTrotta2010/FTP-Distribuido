package connectionserver;

public class Users {

    private String name;
    private String workDir;
    private int port;

    public Users(String name, int port){
        this.name = name;
        this.port = port;
        this.workDir = "Homes/"+this.name+"/";
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public String getWorkDir() {
        return workDir;
    }
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
    
}

package jgabolord.prueba1;

/**
 * Created by Admon on 4/01/2017.
 */


public class marcadores {

    String coor;
    String direc;
    String fecha;

    public marcadores() {

    }

    public marcadores(String coor, String direc, String fecha) {

        // tener en cuenra que son los mismos nombres tal cual utilizados en la base de datos
        this.coor = coor;
        this.direc = direc;
        this.fecha = fecha;
    }

    public String getCoor() {
        return coor;
    }

    public void setCoor(String coor) {
        this.coor = coor;
    }

    public String getDirec() {
        return direc;
    }

    public void setDirec(String direc) {
        this.direc = direc;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
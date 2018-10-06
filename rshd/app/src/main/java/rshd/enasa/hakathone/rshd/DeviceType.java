package rshd.enasa.hakathone.rshd;

class DeviceType {
    String id ;
    String name ;
    double watt;

    public DeviceType(String id, String name, double watt) {
        this.id = id;
        this.name = name;
        this.watt = watt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getWatt() {
        return watt;
    }
}

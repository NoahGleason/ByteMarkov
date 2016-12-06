package net.mrpaul.XA110.ps19;

/**
 * Program created by noah on 12/5/16.
 */
public class BytePlus {

    private boolean extra;
    private byte base;

    public BytePlus(byte base){
        this(base, false);
    }

    public BytePlus(byte base, boolean extra){
        this.base = base;
        this.extra = extra;
    }

    public byte getBase(){
        return base;
    }

    public boolean getExtra(){
        return extra;
    }

    public String toString(){
        if (extra)
            return "e"+Byte.toString(base);
        return Byte.toString(base);
    }
    
    public boolean equals(Object obj){
        return (obj != null) && (obj.getClass() == this.getClass()) && (obj.getBase == base) && (obj.getExtra == extra);
    }
}

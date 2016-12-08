package net.mrpaul.XA110.ps19;

import java.util.Objects;

/**
 * Program created by noah on 12/5/16.
 */
public class BytePlus {
    //An extra bit used to denote special characters.
    private boolean extra;
    //The byte value.
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

    @Override
    public String toString(){
        if (extra)
            return "e"+Byte.toString(base);
        return Byte.toString(base);
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null || obj.getClass() != this.getClass()) {
            System.out.println("obj was not a BytePlus!");
            return false;
        }
        BytePlus other = (BytePlus) obj;
        return (other.getBase() == base) && (other.getExtra() == extra);
    }

    @Override
    public int hashCode(){
        return Objects.hash(base, extra);
    }
}
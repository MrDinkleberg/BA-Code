public class SegmentHeader {

    public static final int MAXBLOCKSIZE_EXPONENT = 31;

    public long startaddress;
    public long endaddress;
    public int maxblocksize;
    public int blocks;
    public long fragmentedblocks;
    public double fragmentation;
    public long[] freeblocks;


    public SegmentHeader(long startaddress, int maxblocksize, long size) {
        this.startaddress = startaddress;
        this.endaddress = startaddress + size;
        this.blocks = (int) (size / maxblocksize + 1);
        freeblocks = new long[MAXBLOCKSIZE_EXPONENT+1];
    }

    public long getFreeBlock(int size){
        return freeblocks[findFittingBlock(size)];
    }

    public int findFittingBlock(int size){

        if(size >= 18 && size <= 39 ) return 0;     //Die ersten Freispeicherlisten besitzen ein anderes Groessenintervall
        if(size >= 40 && size <= 61 ) return 1;     //als die anderen
        if(size >= 62 && size <= 83 ) return 2;
        if(size >= 84 && size <= 105 ) return 3;
        if(size >= 106 && size <= 127 ) return 4;

        for(int i = 5; i < MAXBLOCKSIZE_EXPONENT; i++ ){                //ab der fuenften Liste wird in der i-ten Liste
            if(size >= Math.pow(2, i+2) && size < Math.pow(2, i+3)){    //Bloecke der Groesse 2^(i+1) bis 2^(i+2)-1
                return i;                                               //gespeichert (Array-Indizes, deshalb erste Liste
            }                                                           //bei i=0)
        }

        return MAXBLOCKSIZE_EXPONENT;
    }


}

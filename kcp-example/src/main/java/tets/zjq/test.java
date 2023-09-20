package tets.zjq;

import internal.ReItrLinkedList;
import internal.ReusableListIterator;

import java.util.Iterator;

/**
 * @Description TODO
 * @Author: stem) Zjq
 * @Date: 9:50
 */

public class test {
    public static void main(String[] args) {

        ReItrLinkedList<String> sndBuf = new ReItrLinkedList<>();
        sndBuf.add("1");
        sndBuf.add("2");
        sndBuf.add("3");
        System.out.println(sndBuf.size());
        System.out.println("----------");
        ReusableListIterator<String> sndBufItr = sndBuf.listIterator();

        for (Iterator<String> itr = sndBufItr.rewind(); itr.hasNext(); ) {
            String str = itr.next();
            System.out.println(str);
        }
        System.out.println("----------");
        System.out.println(sndBuf.size());
        System.out.println("----------");
        for (Iterator<String> itr = sndBufItr.rewind(); itr.hasNext(); ) {
            String str = itr.next();
            System.out.println(str);
        }
    }
}

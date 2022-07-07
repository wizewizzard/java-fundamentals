package edu.wz.lists;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ListsTest {
    @Test
    public void listIterator(){
        List<String> data = new ArrayList<>(List.of("Emmmmmmmie", "AnOk", "Coooolio", "Bebebi", "Deeep"));

        var listIterator = data.listIterator();
        listIterator.next();
        listIterator.add("Nunu");

        assertThat(listIterator.next()).isEqualTo("AnOk");
        assertThat(listIterator.previous()).isEqualTo("AnOk");
        listIterator.set("AnIk");
        assertThat(listIterator.nextIndex()).isEqualTo(2);
        assertThat(data).containsSequence(List.of("Emmmmmmmie", "Nunu", "AnIk", "Coooolio", "Bebebi", "Deeep"));

    }

    @Test
    public void otherMethods(){
        List<String> list1 = new ArrayList<>(List.of("Emmmmmmmie", "AnOk", "Coooolio", "Bebebi", "Deeep"));
        List<String> list2 = new ArrayList<>(List.of("Deeep", "AnOk", "Bojjjj", "Quii"));
        List<String> list3 = new ArrayList<>(List.of("Emmmmmmmie", "Deeep", "AnOk"));
        List<String> list4 = new ArrayList<>(list1);
        List<String> list5 = new ArrayList<>(list1);

        list4.retainAll(list2);
        //compare by last char
        list5.sort(Comparator.comparing(s -> s.charAt(s.length() - 1), Character::compareTo));
        String min = Collections.min(list1);

        assertThat(list1.containsAll(list3)).isTrue();
        assertThat(list1.subList(1, 3)).containsExactly("AnOk", "Coooolio");
        assertThat(list4).containsExactlyInAnyOrder("Deeep", "AnOk");
        assertThat(list5).containsExactly("Emmmmmmmie", "Bebebi", "AnOk", "Coooolio", "Deeep");
        assertThat(min).isEqualTo("AnOk");
    }

    @Test
    public void listAddNullValue(){
        List<String> list1 = new ArrayList<>(List.of("Emmmmmmmie", "AnOk", "Coooolio", "Bebebi", "Deeep"));

        list1.add(null);

        assertThat(list1).containsExactly("Emmmmmmmie", "AnOk", "Coooolio", "Bebebi", "Deeep", null);

    }
}

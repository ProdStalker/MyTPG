package com.mytpg.engines.tools;

import com.mytpg.engines.entities.core.EntityWithName;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by stalker-mac on 16.11.14.
 */
public abstract class SearchTools {
    public static List<Integer> searchEntityWithName(String ArgTextToSearch, List<? extends EntityWithName> ArgEntityWithNameList){
        List<Integer> removedPositions = new ArrayList<Integer>();
        int i = ArgEntityWithNameList.size() -1;
        while (i >= 0)
        {
            EntityWithName entityWithName = ArgEntityWithNameList.get(i);
            if (entityWithName != null)
            {
                String name = Normalizer.normalize(entityWithName.getName(), Normalizer.Form.NFD);
                name = name.replaceAll("[^\\p{ASCII}]", "");
                name = name.toLowerCase(Locale.FRENCH);

                String search = Normalizer.normalize(ArgTextToSearch, Normalizer.Form.NFD);
                search = search.replaceAll("[^\\p{ASCII}]", "");
                search = search.toLowerCase(Locale.FRENCH);

                if (!name.contains(search))
                {
                    removedPositions.add(Integer.valueOf(i));
                    ArgEntityWithNameList.remove(i);
                }

            }
            i--;
        }

        return removedPositions;
    }
}

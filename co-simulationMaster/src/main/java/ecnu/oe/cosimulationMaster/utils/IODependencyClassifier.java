package ecnu.oe.cosimulationMaster.utils;

import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;

/**
 * @Author： oe
 * @Description:
 * @Created by oe on 2018/5/17.
 */
public class IODependencyClassifier {
    private static HashMap<String, String>[] sets;
    public static HashMap<String, String>[] classify(int[] errQue, HashMap<String,String> dataExchange_map){
        sets = new HashMap [2];
        sets[0] = new HashMap<String, String>();
        sets[1] = new HashMap<String, String>();
        Iterator entries = dataExchange_map.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry entry = (Map.Entry) entries.next();
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            int fmuIndex = Integer.parseInt(key.substring(0,key.indexOf(".")));
            int j = 0;
            for(int i : errQue)
                if(fmuIndex == i){
                    j++;
                    sets[0].put(key,value);
                }
            if(j==0)
                sets[1].put(key,value);
        }

        return sets;
    }
}

/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.util;

import archimulator.model.ExperimentStat;
import net.pickapack.action.Function1;
import net.pickapack.collection.CollectionHelper;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Jedis helper.
 *
 * @author Min Cai
 */
public class JedisHelper {
    public static final String KEY_PREFIX_EXPERIMENT_STATS = "archimulator.experimentStatService.stats.";

    private static Jedis jedis;

    /**
     * Static constructor.
     */
    static {
        jedis = new Jedis("localhost");
    }

    public static void addStatsByParent(long parentId, final List<ExperimentStat> stats) {
        String prefix = stats.get(0).getPrefix();
        String experimentKey = getKeyByParentAndPrefix(parentId, prefix);

        jedis.hmset(experimentKey, new LinkedHashMap<String, String>() {{
            for (ExperimentStat stat : stats) {
                put(stat.getKey(), stat.getValue());
            }
        }});
    }

    public static void clearStatsByParentAndPrefix(long parentId, String prefix) {
        jedis.del(getKeyByParentAndPrefix(parentId, prefix));
    }

    public static void clearStatsByParent(long parentId) {
        List<String> experimentKeys = getKeysByParent(parentId);

        if(!experimentKeys.isEmpty()) {
            jedis.del(experimentKeys.toArray(new String[experimentKeys.size()]));
        }
    }

    public static void clearAllStats() {
        Set<String> experimentKeys = jedis.keys(KEY_PREFIX_EXPERIMENT_STATS + "*");

        if(!experimentKeys.isEmpty()) {
            jedis.del(experimentKeys.toArray(new String[experimentKeys.size()]));
        }
    }

    public static List<ExperimentStat> getStatsByParent(long parentId) {
        List<String> prefixes = getStatPrefixesByParent(parentId);

        List<ExperimentStat> stats = new ArrayList<ExperimentStat>();

        for(String prefix : prefixes) {
            List<ExperimentStat> result = getStatsByParentAndPrefix(parentId, prefix);
            stats.addAll(result);
        }

        return stats;
    }

    public static List<ExperimentStat> getStatsByParentAndPrefix(long parentId, String prefix) {
        String experimentKey = getKeyByParentAndPrefix(parentId, prefix);
        if(jedis.exists(experimentKey)) {
            List<ExperimentStat> stats = new ArrayList<ExperimentStat>();

            Map<String,String> result = jedis.hgetAll(experimentKey);

            for(String resultKey : result.keySet()) {
                ExperimentStat stat = new ExperimentStat(parentId, prefix, resultKey, result.get(resultKey));
                stats.add(stat);
            }

            return stats;
        }

        return new ArrayList<ExperimentStat>();
    }

    public static ExperimentStat getStatByParentAndPrefixAndKey(long parentId, String prefix, String key) {
        String experimentKey = getKeyByParentAndPrefix(parentId, prefix);
        if(jedis.exists(experimentKey)) {
            return new ExperimentStat(parentId, prefix, experimentKey, jedis.hget(experimentKey, key));
        }

        return null;
    }

    public static List<ExperimentStat> getStatsByParentAndPrefixAndKeyLike(long parentId, String prefix, String keyLike) {
        String experimentKey = getKeyByParentAndPrefix(parentId, prefix);

        List<ExperimentStat> stats = new ArrayList<ExperimentStat>();

        if(jedis.exists(experimentKey)) {
            Set<String> keys = jedis.hkeys(experimentKey);

            for(String key : keys) {
                if(key.contains(keyLike)) {
                    stats.add(new ExperimentStat(parentId, prefix, key, jedis.hget(experimentKey, key)));
                }
            }
        }

        return stats;
    }

    public static List<String> getStatPrefixesByParent(long parentId) {
        List<String> experimentKeys = getKeysByParent(parentId);
        return CollectionHelper.transform(experimentKeys, new Function1<String, String>() {
            @Override
            public String apply(String key) {
                return key.substring(key.indexOf("/") + 1);
            }
        });
    }

    public static List<Integer> getExperimentIds() {
        List<String> experimentKeys = new ArrayList<String>(jedis.keys(KEY_PREFIX_EXPERIMENT_STATS + "*"));
        return CollectionHelper.transform(experimentKeys, new Function1<String, Integer>() {
            @Override
            public Integer apply(String key) {
                return Integer.parseInt(key.substring(key.lastIndexOf(".") + 1, key.indexOf("/")));
            }
        });
    }

    public static List<String> getKeysByParent(long parentId) {
        return new ArrayList<String>(jedis.keys(KEY_PREFIX_EXPERIMENT_STATS + parentId + "*"));
    }

    public static String getKeyByParentAndPrefix(long parentId, String prefix) {
        return KEY_PREFIX_EXPERIMENT_STATS + parentId + "/" + prefix;
    }
}

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
import net.pickapack.io.serialization.JsonSerializationHelper;
import net.pickapack.util.Pair;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Jedis helper.
 *
 * @author Min Cai
 */
public class JedisHelper {
    public static final String KEY_PREFIX_EXPERIMENT_STATS = "archimulator.experimentStatService.stats.";

    private static final long MAX_TICK = 1000000000L;

    private static Jedis jedis;

    private static long currentTick;

    /**
     * Static constructor.
     */
    static {
        jedis = new Jedis("localhost", Protocol.DEFAULT_PORT, 180000);
    }

    /**
     * Add a list of statistics under the specified parent experiment object.
     *
     * @param parentId the ID of the parent experiment object
     * @param stats    a list of statistics to be added under the specified parent
     */
    public static void addStatsByParent(long parentId, final List<ExperimentStat> stats) {
        String prefix = stats.get(0).getPrefix();
        String experimentKey = getKeyByParentAndPrefix(parentId, prefix);

        jedis.hmset(experimentKey, new LinkedHashMap<String, String>() {{
            for (ExperimentStat stat : stats) {
                put(stat.getKey(), JsonSerializationHelper.serialize(new Pair<>(stat.getValue(), "" + currentTick++)));
            }
        }});

        checkForResetCurrentTick();
    }

    /**
     * Check for resetting the current tick.
     */
    private static void checkForResetCurrentTick() {
        if(currentTick > MAX_TICK) {
            currentTick = 0;
        }
    }

    /**
     * Clear the list of statistics under the specified parent experiment object and the specified prefix.
     *
     * @param parentId the ID of the parent experiment object
     * @param prefix   the prefix
     */
    public static void clearStatsByParentAndPrefix(long parentId, String prefix) {
        jedis.del(getKeyByParentAndPrefix(parentId, prefix));
    }

    /**
     * Clear the list of statistics under the specified parent experiment object.
     *
     * @param parentId the ID of the parent experiment object
     */
    public static void clearStatsByParent(long parentId) {
        Set<String> experimentKeys = getKeysByParent(parentId);

        if (!experimentKeys.isEmpty()) {
            jedis.del(experimentKeys.toArray(new String[experimentKeys.size()]));
        }
    }

    /**
     * Clear all the statistics stored in the Redis database.
     */
    public static void clearAllStats() {
        Set<String> experimentKeys = jedis.keys(KEY_PREFIX_EXPERIMENT_STATS + "*");

        if (!experimentKeys.isEmpty()) {
            jedis.del(experimentKeys.toArray(new String[experimentKeys.size()]));
        }
    }

    /**
     * Get the list of statistics under the specified parent experiment object and matching the specified title prefix.
     *
     * @param parentId the ID of the parent experiment object
     * @param prefix the title prefix
     * @return a list of statistics under the specified parent experiment object and matching the specified title prefix if any exist; otherwise an empty list
     */
    @SuppressWarnings("unchecked")
    public static List<ExperimentStat> getStatsByParentAndPrefix(long parentId, String prefix) {
        String experimentKey = getKeyByParentAndPrefix(parentId, prefix);
        if (jedis.exists(experimentKey)) {
            List<Pair<ExperimentStat, String>> stats = new ArrayList<>();

            Map<String, String> result = jedis.hgetAll(experimentKey);

            for (String resultKey : result.keySet()) {
                Pair<String, String> value = JsonSerializationHelper.deserialize(Pair.class, result.get(resultKey));

                ExperimentStat stat = new ExperimentStat(parentId, prefix, resultKey, value.getFirst());
                stats.add(new Pair<>(stat, value.getSecond()));
            }

            Collections.sort(stats, (o1, o2) -> {
                Double first = Double.parseDouble(o1.getSecond());
                Double second = Double.parseDouble(o2.getSecond());
                return first.compareTo(second);
            });

            return stats.stream().map(Pair::getFirst).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * Get the statistics under the specified parent experiment object and matching the specified prefix and key.
     *
     * @param parentId the ID of the parent experiment object
     * @param prefix the prefix
     * @param key    the key
     * @return the statistics under the specified parent experiment object and matching the specified prefix and key if any exist; otherwise an empty list
     */
    @SuppressWarnings("unchecked")
    public static ExperimentStat getStatByParentAndPrefixAndKey(long parentId, String prefix, String key) {
        String experimentKey = getKeyByParentAndPrefix(parentId, prefix);
        if (jedis.exists(experimentKey)) {
            Pair<String, String> value = JsonSerializationHelper.deserialize(Pair.class, jedis.hget(experimentKey, key));
            return new ExperimentStat(parentId, prefix, experimentKey, value.getFirst());
        }

        return null;
    }

    /**
     * Get the list of statistics under the specified parent experiment object and matching the specified prefix and key pattern.
     *
     * @param parentId the ID of the parent experiment object
     * @param prefix the prefix
     * @param keyLike the key pattern
     * @return a list of statistics under the specified parent experiment object and matching the specified prefix and key pattern if any exist; otherwise an empty list
     */
    @SuppressWarnings("unchecked")
    public static List<ExperimentStat> getStatsByParentAndPrefixAndKeyLike(long parentId, String prefix, String keyLike) {
        String experimentKey = getKeyByParentAndPrefix(parentId, prefix);

        List<Pair<ExperimentStat, String>> stats = new ArrayList<>();

        if (jedis.exists(experimentKey)) {
            Set<String> keys = jedis.hkeys(experimentKey);

            for (String key : keys) {
                if (key.contains(keyLike)) {
                    Pair<String, String> value = JsonSerializationHelper.deserialize(Pair.class, jedis.hget(experimentKey, key));

                    ExperimentStat stat = new ExperimentStat(parentId, prefix, key, value.getFirst());
                    stats.add(new Pair<>(stat, value.getSecond()));
                }
            }
        }

        Collections.sort(stats, (o1, o2) -> {
            Double first = Double.parseDouble(o1.getSecond());
            Double second = Double.parseDouble(o2.getSecond());
            return first.compareTo(second);
        });

        return stats.stream().map(Pair::getFirst).collect(Collectors.toList());
    }

    /**
     * Get the list of statistic prefixes under the specified parent experiment object.
     *
     * @param parentId the ID of the parent experiment object
     * @return a list of statistic prefixes under the specified parent experiment object
     */
    public static List<String> getStatPrefixesByParent(long parentId) {
        List<String> experimentKeys = new ArrayList<>(getKeysByParent(parentId));
        return experimentKeys.stream().map(key -> key.substring(key.indexOf("/") + 1)).collect(Collectors.toList());
    }

    /**
     * Get the list of experiment IDs stored in the Redis database.
     *
     * @return the list of experiment IDs stored in the Redis database
     */
    public static List<Long> getExperimentIds() {
        List<String> experimentKeys = new ArrayList<>(jedis.keys(KEY_PREFIX_EXPERIMENT_STATS + "*"));
        return experimentKeys.stream().map(key -> Long.parseLong(key.substring(key.lastIndexOf(".") + 1, key.indexOf("/")))).collect(Collectors.toList());
    }

    /**
     * Get the set of keys by the specified parent.
     *
     * @param parentId the ID of the parent
     *
     * @return the set of keys matched by the specified parent
     */
    private static Set<String> getKeysByParent(long parentId) {
        return jedis.keys(KEY_PREFIX_EXPERIMENT_STATS + parentId + "*");
    }

    /**
     * Get the key matching the specified parent and prefix.
     *
     * @param parentId the ID of the parent
     * @param prefix the prefix
     *
     * @return the key matching the specified parent and prefix
     */
    private static String getKeyByParentAndPrefix(long parentId, String prefix) {
        return KEY_PREFIX_EXPERIMENT_STATS + parentId + "/" + prefix;
    }
}

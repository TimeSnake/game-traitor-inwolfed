/*
 * game-traitor-inwolfed.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.game.traitor_inwolfed.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.loungebridge.util.game.ItemSpawner;
import de.timesnake.basic.loungebridge.util.game.ResetableMap;
import de.timesnake.basic.loungebridge.util.tool.Timeable;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.game.traitor_inwolfed.main.Plugin;
import org.bukkit.GameRule;

import java.util.List;

public class TraitorInwolfedMap extends Map implements ResetableMap, Timeable {

    public static final int DEFAULT_TIME = 5 * 60;

    private static final int SPECTATOR_LOCATION_INDEX = 0;
    private static final int SPAWN_LOCATION_INDEX = 1;

    private static final int ITEM_SPAWNERS_START_INDEX = 100;
    private static final int ITEM_SPAWNERS_END_INDEX = 200;

    private static final int TELEPORTER_START_INDEX = 200;
    private static final int TELEPORTER_END_INDEX = 300;

    private final List<ExLocation> teleporterLocations;

    private final int time;

    public TraitorInwolfedMap(DbMap map, boolean loadWorld) {
        super(map, loadWorld);

        int time = DEFAULT_TIME;

        for (String info : super.getInfo()) {
            String key = info.split("=")[0];
            String value = info.split("=")[1];

            if (key.equalsIgnoreCase("time")) {
                try {
                    time = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    Server.printWarning(Plugin.TRAITOR_INWOLFED, "Can not load time of map " + this.getName(),
                            "Map");
                }
            }
        }

        this.time = time;

        this.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.getWorld().setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        this.getWorld().setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        this.getWorld().restrict(ExWorld.Restriction.BLOCK_BREAK, true);
        this.getWorld().restrict(ExWorld.Restriction.BLOCK_PLACE, true);
        this.getWorld().restrict(ExWorld.Restriction.CAKE_EAT, false);
        this.getWorld().restrict(ExWorld.Restriction.ENTITY_EXPLODE, true);
        this.getWorld().restrict(ExWorld.Restriction.FIRE_SPREAD, true);
        this.getWorld().restrict(ExWorld.Restriction.FLUID_PLACE, true);
        this.getWorld().restrict(ExWorld.Restriction.FLUID_COLLECT, true);
        this.getWorld().restrict(ExWorld.Restriction.ENTITY_BLOCK_BREAK, true);
        this.getWorld().restrict(ExWorld.Restriction.PLACE_IN_BLOCK, true);
        this.getWorld().setExceptService(true);

        for (int index : this.getLocationsIds(ITEM_SPAWNERS_START_INDEX, ITEM_SPAWNERS_END_INDEX)) {
            TraitorInwolfedServer.getToolManager().add(this, new ItemSpawner(index, TraitorInwolfedServer.SPAWNER_DELAY,
                    TraitorInwolfedServer.SPAWNER_RANGE, TraitorInwolfedServer.SPAWNER_ITEMS));
        }

        this.teleporterLocations = this.getLocations(TELEPORTER_START_INDEX, TELEPORTER_END_INDEX);
        if (this.teleporterLocations.isEmpty()) {
            Server.printWarning(Plugin.TRAITOR_INWOLFED, "No teleporter locations found for map " + this.getName());
        }
    }

    public ExLocation getSpawnLocation() {
        return super.getLocation(SPAWN_LOCATION_INDEX);
    }

    public ExLocation getSpectatorLocation() {
        return super.getLocation(SPECTATOR_LOCATION_INDEX);
    }

    public ExLocation getRandomTeleport() {
        if (this.teleporterLocations.isEmpty()) {
            return null;
        }
        return this.teleporterLocations.get(Server.getRandom().nextInt(this.teleporterLocations.size()));
    }

    @Override
    public int getTime() {
        return time;
    }
}

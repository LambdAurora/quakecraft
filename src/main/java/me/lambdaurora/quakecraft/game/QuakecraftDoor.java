/*
 * Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lambdaurora.quakecraft.game;

import me.lambdaurora.quakecraft.block.TeamBarrierBlock;
import me.lambdaurora.quakecraft.game.map.QuakecraftMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents a door which opens/closes automatically.
 *
 * @author LambdAurora
 * @version 1.5.0
 * @since 1.5.0
 */
public class QuakecraftDoor
{
    private final QuakecraftLogic game;
    private final TemplateRegion region;
    private final BlockBounds bounds;
    private final BlockBounds detectionBounds;
    private final BlockBounds exitDetectionBounds;
    private final Direction facing;
    private final BlockState openState;
    private final BlockState closedState;
    private final GameTeam team;
    private boolean open = false;
    private int openTicks = 0;

    public QuakecraftDoor(@NotNull QuakecraftLogic game,
                          @NotNull TemplateRegion region,
                          @NotNull BlockBounds bounds, @NotNull BlockBounds detectionBounds, @NotNull BlockBounds exitDetectionBounds,
                          @NotNull Direction facing, @NotNull BlockState closedState,
                          @Nullable GameTeam team)
    {
        this.game = game;
        this.region = region;
        this.bounds = bounds;
        this.detectionBounds = detectionBounds;
        this.exitDetectionBounds = exitDetectionBounds;
        this.facing = facing;
        this.openState = TeamBarrierBlock.of(team).getDefaultState();
        this.closedState = closedState;
        this.team = team;
    }

    /**
     * Returns the region assigned to this door.
     *
     * @return The region.
     */
    public @NotNull TemplateRegion getRegion()
    {
        return this.region;
    }

    /**
     * The bounds of the door.
     * <p>
     * All positions inside those bounds are replaced with blocks whether the door is closed or not.
     *
     * @return The bounds of the door.
     */
    public @NotNull BlockBounds getBounds()
    {
        return this.bounds;
    }

    /**
     * Returns the detection bounds. The door will open if any allowed player is in the detection bounds.
     *
     * @return The detection bounds.
     */
    public @NotNull BlockBounds getDetectionBounds()
    {
        return this.detectionBounds;
    }

    /**
     * Returns the bounds where any players should open the door to be able to exit.
     *
     * @return The exit detection bounds.
     */
    public @NotNull BlockBounds getExitDetectionBounds()
    {
        return this.exitDetectionBounds;
    }

    /**
     * Returns the team assigned to this door. The team mays be null.
     *
     * @return The assigned team.
     */
    public GameTeam getTeam()
    {
        return this.team;
    }

    public void tick()
    {
        List<ServerPlayerEntity> players = this.game.getWorld().getWorld().getEntitiesByClass(ServerPlayerEntity.class, this.detectionBounds.toBox(),
                player -> this.game.canOpenDoor(this, player));
        if (players.size() > 0) {
            if (!this.open) {
                this.open();
            }
            this.openTicks = 2;
        }

        if (this.openTicks == 0) {
            this.close();
        } else this.openTicks--;
    }

    /**
     * Opens the door.
     */
    public void open()
    {
        this.getBounds().iterate().forEach(pos -> this.game.getWorld().getWorld().setBlockState(pos, this.openState, 0b0111010));
        this.open = true;
    }

    /**
     * Closes the door.
     */
    public void close()
    {
        this.getBounds().iterate().forEach(pos -> this.game.getWorld().getWorld().setBlockState(pos, this.closedState, 0b0111010));
        this.open = false;
    }

    public static @NotNull Optional<QuakecraftDoor> fromRegion(@NotNull QuakecraftLogic game, @NotNull TemplateRegion region)
    {
        BlockBounds bounds = region.getBounds().offset(QuakecraftMap.ORIGIN);

        String serializedDirection = region.getData().getString("facing");
        Direction facing = Arrays.stream(Direction.values()).filter(direction -> direction.getName().equals(serializedDirection)).findFirst().orElse(null);
        if (facing == null)
            return Optional.empty();

        int distance = region.getData().getInt("distance");
        if (distance == 0)
            return Optional.empty();

        BlockPos min = bounds.getMin().offset(facing.getOpposite(), distance);
        BlockPos max = bounds.getMax().offset(facing, distance);

        // A block must be explicitly defined.
        if (!region.getData().getCompound("block").contains("Name"))
            return Optional.empty();
        BlockState closedState = NbtHelper.toBlockState(region.getData().getCompound("block"));

        GameTeam team = game.getTeam(region.getData().getString("team"));

        QuakecraftDoor door = new QuakecraftDoor(game, region, bounds, new BlockBounds(min, max), new BlockBounds(bounds.getMin(), max),
                facing, closedState, team);
        door.close();
        return Optional.of(door);
    }
}
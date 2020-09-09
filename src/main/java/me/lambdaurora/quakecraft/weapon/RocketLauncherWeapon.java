/*
 *  Copyright (c) 2020 LambdAurora <aurora42lambda@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lambdaurora.quakecraft.weapon;

import me.lambdaurora.quakecraft.entity.RocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.GameWorld;

/**
 * Represents a rocket launcher.
 *
 * @author LambdAurora
 * @version 1.3.0
 * @since 1.3.0
 */
public class RocketLauncherWeapon extends Weapon
{
    public RocketLauncherWeapon(@NotNull Item item, int primaryCooldown)
    {
        super(item, primaryCooldown);
    }

    @Override
    public @NotNull ActionResult onPrimary(@NotNull GameWorld world, @NotNull ServerPlayerEntity player, @NotNull Hand hand)
    {
        RocketEntity rocket = new RocketEntity(world.getWorld(), player, 0, 0, 0);
        rocket.setProperties(player, player.pitch, player.yaw, 0.f, 1.5f, 1.f);
        rocket.setVelocity(rocket.getVelocity().multiply(0.75));
        rocket.setItem(new ItemStack(Items.FIRE_CHARGE));
        rocket.rollCritical();
        world.getWorld().spawnEntity(rocket);

        return super.onPrimary(world, player, hand);
    }
}

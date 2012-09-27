package edgruberman.bukkit.waterfix;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

import edgruberman.bukkit.waterfix.util.CustomPlugin;

public final class Main extends CustomPlugin implements Listener {

    private static final BlockFace[] ADJACENTS = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "2.0.0a33"); }

    @Override
    public void onEnable() {
        this.reloadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent event) {
        final Block from = event.getBlock();
        final Block to = event.getToBlock();

        // from block must be a full water source
        if (!this.isWater(from.getTypeId())) return;
        if (this.getLogger().isLoggable(Level.FINEST)) {
            this.getLogger().log(Level.FINEST, "from: {0},{1},{2} to: {3},{4},{5} | from: {6}/{7}({8}) to: {9}/{10}({11})", new Object[] {
                      from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ()
                    , from.getType(), FluidLevel.getByData(from.getData()).name(), from.getData()
                    , to.getType(), FluidLevel.getByData(to.getData()).name(), to.getData()
            });
        }
        if (FluidLevel.getByData(from.getData()) != FluidLevel.FULL) return;

        // must be air or non-full water to continue checking for fix
        if (!(to.getTypeId() == Material.AIR.getId() || (this.isWater(to.getTypeId()) && FluidLevel.getByData(to.getData()) != FluidLevel.FULL))) return;

        if (!this.fix(to, from)) return;

        event.setCancelled(true);
        if (this.getLogger().isLoggable(Level.FINEST)) {
            final FluidLevel level = FluidLevel.getByData(to.getState().getRawData());
            this.getLogger().log(Level.FINEST, "++ Created Source ++ WATER/{0}({1}) at {2}", new Object[] { level, level.getData(), to });
        }
    }

    private boolean fix(final Block block, final Block source) {
        int sources = ( source == null ? 0 : 1 );
        for (final BlockFace direction : Main.ADJACENTS) {
            final int adjacentX = block.getX() + direction.getModX();
            final int adjacentY = block.getY() + direction.getModY();
            final int adjacentZ = block.getZ() + direction.getModZ();

            // when original source block already identified, skip further checks for it
            if (source != null && source.getX() == adjacentX && source.getY() == adjacentY && source.getZ() == adjacentZ) continue;

            // check block type id first to avoid more expensive block data call if possible
            if (!this.isWater(block.getWorld().getBlockTypeIdAt(adjacentX, adjacentY, adjacentZ))) continue;

            // block data must indicate water block is full
            if (FluidLevel.getByData(block.getRelative(direction).getData()) != FluidLevel.FULL) continue;

            sources++;

            // at least two full water source blocks must be directly adjacent to destination block to create a new full water source block
            if (sources == 2) {
                final int other = ~FluidLevel.DATA_MASK & block.getData();
                final int data = other | FluidLevel.FULL.getData();
                block.setTypeIdAndData(Material.WATER.getId(), (byte) data, true);
                return true;
            }
        }

        return false;
    }

    private boolean isWater(final int id) {
        return (id == Material.WATER.getId() || id == Material.STATIONARY_WATER.getId());
    }

}

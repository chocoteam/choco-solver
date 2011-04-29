package choco.kernel.memory.trailing.trail;

import choco.kernel.memory.structure.Operation;
import choco.kernel.memory.trailing.EnvironmentTrailing;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 fŽvr. 2011
 */
public class OperationTrail implements ITrailStorage {


    /**
     * Stack of values (former values that need be restored upon backtracking).
     */

    private Operation[] valueStack;


    /**
     * Points the level of the last entry.
     */

    private int currentLevel;


    /**
     * A stack of pointers (for each start of a world).
     */

    private int[] worldStartLevels;

    /**
     * capacity of the trailing stack (in terms of number of updates that can be stored)
     */
    private int maxUpdates = 0;


    /**
     * Constructs a trail with predefined size.
     *
     * @param nUpdates maximal number of updates that will be stored
     * @param nWorlds  maximal number of worlds that will be stored
     */

    public OperationTrail(EnvironmentTrailing env, int nUpdates, int nWorlds) {
        currentLevel = 0;
        maxUpdates = nUpdates;
        valueStack = new Operation[maxUpdates];
        worldStartLevels = new int[nWorlds];
    }


    /**
     * Moving up to the next world.
     *
     * @param worldIndex
     */

    public void worldPush(int worldIndex) {
        worldStartLevels[worldIndex] = currentLevel;
    }


    /**
     * Moving down to the previous world.
     *
     * @param worldIndex
     */

    public void worldPop(int worldIndex) {
        final int wsl = worldStartLevels[worldIndex];
        while (currentLevel > wsl) {
            currentLevel--;
            valueStack[currentLevel].undo();
        }
    }


    /**
     * Returns the current size of the stack.
     */

    public int getSize() {
        return currentLevel;
    }


    /**
     * Comits a world: merging it with the previous one.
     */

    public void worldCommit() {
    }

    /**
     * Reacts when a StoredInt is modified: push the former value & timestamp
     * on the stacks.
     */
    public void savePreviousState(Operation oldValue) {
        valueStack[currentLevel] = oldValue;
        currentLevel++;
        if (currentLevel == maxUpdates) {
            resizeUpdateCapacity();
        }
    }

    private void resizeUpdateCapacity() {
        final int newCapacity = ((maxUpdates * 3) / 2);
        // First, copy the stack of former values
        final Operation[] tmp2 = new Operation[newCapacity];
        System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
        valueStack = tmp2;
        // last update the capacity
        maxUpdates = newCapacity;
    }

    public void resizeWorldCapacity(int newWorldCapacity) {
        final int[] tmp = new int[newWorldCapacity];
        System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
        worldStartLevels = tmp;
    }
}

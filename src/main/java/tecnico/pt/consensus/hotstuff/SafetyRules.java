package tecnico.pt.consensus.hotstuff;

import tecnico.pt.consensus.hotstuff.model.Block;
import tecnico.pt.consensus.hotstuff.model.QC;

public final class SafetyRules {

    private SafetyRules() {}

    /**
     * Step 3: keep it simple (still correct structure).
     *
     * Vote if:
     * - B extends lockedQC.block OR
     * - B.justify.view > lockedQC.view
     */
    public static boolean safeToVote(HotStuffState st, Block b) {
        QC locked = st.getLockedQC();

        // Genesis/base case
        if (locked == null) return true;

        boolean extendsLocked = extendsBlockId(st, b, locked.getBlockId());
        boolean newerJustify = b.getJustify() != null && b.getJustify().getView() > locked.getView();

        return extendsLocked || newerJustify;
    }

    private static boolean extendsBlockId(HotStuffState st, Block b, String ancestorId) {
        String cur = b.getId();
        while (cur != null) {
            if (cur.equals(ancestorId)) return true;
            var blkOpt = st.getBlockStore().get(cur);
            if (blkOpt.isEmpty()) return false;
            cur = blkOpt.get().getParentId();
        }
        return false;
    }
}
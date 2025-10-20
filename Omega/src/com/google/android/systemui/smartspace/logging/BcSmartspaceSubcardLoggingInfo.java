package com.google.android.systemui.smartspace.logging;

import java.util.List;
import java.util.Objects;

public final class BcSmartspaceSubcardLoggingInfo {
    public List<BcSmartspaceCardMetadataLoggingInfo> mSubcards;
    public int mClickedSubcardIndex;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BcSmartspaceSubcardLoggingInfo)) {
            return false;
        }
        BcSmartspaceSubcardLoggingInfo other = (BcSmartspaceSubcardLoggingInfo) obj;
        return mClickedSubcardIndex == other.mClickedSubcardIndex
                && Objects.equals(mSubcards, other.mSubcards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mSubcards, mClickedSubcardIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BcSmartspaceSubcardLoggingInfo{mSubcards=");
        sb.append(mSubcards);
        sb.append(", mClickedSubcardIndex=");
        sb.append(mClickedSubcardIndex);
        sb.append('}');
        return sb.toString();
    }
}

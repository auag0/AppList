package android.content.pm;

import java.util.List;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(PackageManager.class)
public class PackageManagerHidden {
    public List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
        throw new RuntimeException("Stub!");
    }
}
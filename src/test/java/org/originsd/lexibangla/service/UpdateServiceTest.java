package org.originsd.lexibangla.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateServiceTest {

    @Test
    @DisplayName("Verify Semantic Version comparison logic")
    void testSemanticVersioning() {
        assertTrue(UpdateService.isNewerVersion("1.0.1", "1.0.2"));
        assertTrue(UpdateService.isNewerVersion("1.0.1-SNAPSHOT", "1.0.2"));
        assertTrue(UpdateService.isNewerVersion("1.0.1", "1.1.0"));
        assertTrue(UpdateService.isNewerVersion("1.0.1", "2.0.0"));
        assertTrue(UpdateService.isNewerVersion("v1.0.1", "v1.0.2"));

        assertFalse(UpdateService.isNewerVersion("1.0.1", "1.0.1"));
        assertFalse(UpdateService.isNewerVersion("1.0.1-SNAPSHOT", "1.0.1"));
        assertFalse(UpdateService.isNewerVersion("1.0.1", "1.0.1-SNAPSHOT"));
        assertFalse(UpdateService.isNewerVersion("1.0.1-SNAPSHOT", "v1.0.1"));
        assertFalse(UpdateService.isNewerVersion("v1.0.1", "1.0.1"));
        assertFalse(UpdateService.isNewerVersion("1.0.1", "v1.0.1"));
        assertFalse(UpdateService.isNewerVersion("1.1.0", "1.0.1"));
        assertFalse(UpdateService.isNewerVersion("1.0.2", "1.0.1"));
        assertFalse(UpdateService.isNewerVersion("2.0.0", "1.9.9"));
    }

    @Test
    @DisplayName("Verify platform asset matching")
    void testAssetMatching() {
        ReleaseInfo release = new ReleaseInfo();
        ReleaseInfo.ReleaseAsset winAsset = new ReleaseInfo.ReleaseAsset();
        winAsset.setName("LexiBangla-1.0.1-setup.exe");

        ReleaseInfo.ReleaseAsset linuxAsset = new ReleaseInfo.ReleaseAsset();
        linuxAsset.setName("lexibangla_1.0.1_amd64.deb");

        ReleaseInfo.ReleaseAsset macAsset = new ReleaseInfo.ReleaseAsset();
        macAsset.setName("LexiBangla-1.0.1.dmg");

        release.setAssets(List.of(winAsset, linuxAsset, macAsset));

        Optional<ReleaseInfo.ReleaseAsset> matched = UpdateService.findPlatformAsset(release);
        assertTrue(matched.isPresent(), "Should match an asset for the current OS");
    }
}

/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.apkeditor;

import com.reandroid.app.AndroidManifest;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.archive.ArchiveEntry;
import com.reandroid.archive.ArchiveFile;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.apk.ApkBundle;
import com.reandroid.apk.ApkModule;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ValueType;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class BundleMerger {

    public static File mergeBundle(File dir) throws IOException {
        return mergeBundle(dir, null);
    }

    public static File mergeBundle(File dir, String resDirName) throws IOException {
        boolean extracted = false;
        File outputFile = generateOutputFromInput(dir);
        if (dir.isFile()) {
            dir = extractFile(dir);
            extracted = true;
        }
        ApkBundle bundle = new ApkBundle();
        bundle.loadApkDirectory(dir, extracted);
        for (ApkModule apkModule : bundle.getApkModuleList()) {
            String protect = Util.isProtected(apkModule);
            if (protect != null) {
                return dir;
            }
        }
        ApkModule mergedModule = bundle.mergeModules(true);
        if (resDirName != null) {
            mergedModule.setResourcesRootDir(resDirName);
        }
        mergedModule.validateResourcesDir();
        clearMeta(mergedModule);
        sanitizeManifest(mergedModule);
        mergedModule.refreshTable();
        mergedModule.refreshManifest();
        mergedModule.writeApk(outputFile);
        mergedModule.close();
        bundle.close();
        if (extracted) {
            Util.deleteDir(dir);
            dir.deleteOnExit();
        }
        return outputFile;
    }

    private static File generateOutputFromInput(File file) {
        String name = file.getName();
        if (file.isFile()) {
            int i = name.lastIndexOf('.');
            if(i > 0){
                name = name.substring(0, i);
            }
        }
        name = name + "_merged.apk";
        File dir = file.getParentFile();
        if (dir == null) {
            return new File(name);
        }
        return new File(dir, name);
    }

    private static File extractFile(File file) throws IOException {
        File tmp = toTmpDir(file);
        if (tmp.exists()) {
            Util.deleteDir(tmp);
        }
        tmp.deleteOnExit();
        ArchiveFile archive = new ArchiveFile(file);
        fixFilePermissions(archive);
        Predicate <ArchiveEntry> filter = archiveEntry -> archiveEntry.getName().endsWith(".apk");
        int count = archive.extractAll(tmp, filter, null);
        archive.close();
        if(count == 0){
            throw new IOException("No *.apk files found on: " + file);
        }
        return tmp;
    }
    private static void fixFilePermissions(ArchiveFile archive) {
        int rw_all = 438; // equivalent to chmod 666
        Iterator<ArchiveEntry> iterator = archive.iterator();
        while (iterator.hasNext()) {
            ArchiveEntry entry = iterator.next();
            entry.getCentralEntryHeader()
                .getFilePermissions().permissions(rw_all);
        }
    }
    private static File toTmpDir(File file){
        String name = file.getName();
        name = HexUtil.toHex8("tmp_", name.hashCode());
        File dir = file.getParentFile();
        File tmp;
        if (dir == null) {
            tmp = new File(name);
        } else {
            tmp = new File(dir, name);
        }
        tmp = Util.ensureUniqueFile(tmp);
        return tmp;
    }
    private static void sanitizeManifest(ApkModule apkModule) {
        if (!apkModule.hasAndroidManifest()) {
            return;
        }
        AndroidManifestBlock manifest = apkModule.getAndroidManifest();
        AndroidManifestHelper.removeAttributeFromManifestById(manifest,
            AndroidManifest.ID_requiredSplitTypes);
        AndroidManifestHelper.removeAttributeFromManifestById(manifest,
            AndroidManifest.ID_splitTypes);
        AndroidManifestHelper.removeAttributeFromManifestByName(manifest,
            AndroidManifest.NAME_splitTypes);

        AndroidManifestHelper.removeAttributeFromManifestByName(manifest,
            AndroidManifest.NAME_requiredSplitTypes);
        AndroidManifestHelper.removeAttributeFromManifestByName(manifest,
            AndroidManifest.NAME_splitTypes);
        AndroidManifestHelper.removeAttributeFromManifestAndApplication(manifest,
            AndroidManifest.ID_extractNativeLibs);
        AndroidManifestHelper.removeAttributeFromManifestAndApplication(manifest,
            AndroidManifest.ID_isSplitRequired);
        ResXmlElement application = manifest.getApplicationElement();
        List<ResXmlElement> splitMetaDataElements =
        AndroidManifestHelper.listSplitRequired(application);
        boolean splits_removed = false;
        for (ResXmlElement meta : splitMetaDataElements){
            if (!splits_removed) {
                splits_removed = removeSplitsTableEntry(meta, apkModule);
            }
            application.remove(meta);
        }
        manifest.refresh();
    }
    private static boolean removeSplitsTableEntry(ResXmlElement metaElement, ApkModule apkModule) {
        ResXmlAttribute nameAttribute = metaElement.searchAttributeByResourceId(AndroidManifest.ID_name);
        if (nameAttribute == null) {
            return false;
        }
        if (!"com.android.vending.splits".equals(nameAttribute.getValueAsString())) {
            return false;
        }
        ResXmlAttribute valueAttribute=metaElement.searchAttributeByResourceId(
                AndroidManifest.ID_value);
        if (valueAttribute==null) {
            valueAttribute=metaElement.searchAttributeByResourceId(
                AndroidManifest.ID_resource);
        }
        if (valueAttribute == null
            || valueAttribute.getValueType() != ValueType.REFERENCE) {
            return false;
        }
        if (!apkModule.hasTableBlock()) {
            return false;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        ResourceEntry resourceEntry = tableBlock.getResource(valueAttribute.getData());
        if (resourceEntry == null) {
            return false;
        }
        ZipEntryMap zipEntryMap = apkModule.getZipEntryMap();
        for (Entry entry : resourceEntry) {
            if (entry == null) {
                continue;
            }
            ResValue resValue = entry.getResValue();
            if (resValue == null) {
                continue;
            }
            String path = resValue.getValueAsString();
            //Remove file entry
            zipEntryMap.remove(path);
            // It's not safe to destroy entry, resource id might be used in dex code.
            // Better replace it with boolean value.
            entry.setNull(true);
            SpecTypePair specTypePair = entry.getTypeBlock()
                .getParentSpecTypePair();
            specTypePair.removeNullEntries(entry.getId());
        }
        return true;
    }

    private static void clearMeta(ApkModule module){
        removeSignature(module);
        module.setApkSignatureBlock(null);
    }

    private static void removeSignature(ApkModule module){
        ZipEntryMap archive = module.getZipEntryMap();
        archive.removeIf(Pattern.compile("^META-INF/.+\\.(([MS]F)|(RSA))"));
        archive.remove("stamp-cert-sha256");
    }
}

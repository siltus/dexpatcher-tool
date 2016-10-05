/*
 * DexPatcher - Copyright 2015, 2016 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.multidex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

public class DirectoryDexContainer implements MultiDexContainer<DirectoryDexContainer.DirectoryDexFile> {

	private final File directory;
	private final DexFileNamer namer;
	private final Opcodes opcodes;

	public DirectoryDexContainer(File directory, DexFileNamer namer, Opcodes opcodes) {
		this.directory = directory;
		this.namer = namer;
		this.opcodes = opcodes;
	}

	@Override
	public List<String> getDexEntryNames() throws IOException {
		String[] names = directory.list();
		Arrays.sort(names);
		if (names == null) throw new IOException("Not a directory: " + directory);
		List<String> entryNames = new ArrayList<>();
		for (String name : names) {
			if (namer.isValidName(name)) entryNames.add(name);
		}
		return entryNames;
	}

	@Override
	public DirectoryDexFile getEntry(String entryName) throws IOException {
		if (!namer.isValidName(entryName)) return null;
		File file = new File(directory, entryName);
		DexFile dexFile = MultiDexIO.readRawDexFile(file, opcodes);
		return new DirectoryDexFile(dexFile, entryName);
	}

	public class DirectoryDexFile implements MultiDexContainer.MultiDexFile {

		private final DexFile dexFile;
		private final String entryName;

		private DirectoryDexFile(DexFile dexFile, String entryName) {
			this.dexFile = dexFile;
			this.entryName = entryName;
		}

		@Override
		public Set<? extends ClassDef> getClasses() {
			return dexFile.getClasses();
		}

		@Override
		public Opcodes getOpcodes() {
			return dexFile.getOpcodes();
		}

		@Override
		public String getEntryName() {
			return entryName;
		}

		@Override
		public MultiDexContainer<DirectoryDexFile> getContainer() {
			return DirectoryDexContainer.this;
		}

	}

}

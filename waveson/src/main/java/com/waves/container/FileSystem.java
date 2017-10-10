package com.waves.container;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.waves.core.AbstractWavesContainer;
import com.waves.model.Component;
import com.waves.model.Container;
import com.waves.model.Interface;
import com.waves.model.Macro;
import com.waves.process.CacheProcessor;

public class FileSystem extends AbstractWavesContainer {

	Logger log = LoggerFactory.getLogger(FileSystem.class);

	Map<String, List<Path>> files = new HashMap<>();

	private String root = null;

	public FileSystem(Container container) {
		super(container);
		this.root = container.getPath();
	}

	@Override
	public boolean create(Container container) throws IOException {
		return false;
	}

	@Override
	public void build(Component comp) throws Exception {

		for (Interface i : comp.getInterface()) {

			Path path = Paths.get(root, comp.getPath(), i.getOn());
			String access_path = i.getOn();
			String match = "*.*";

			if (i.getMacros() != null) {
				for (Macro macro : i.getMacros()) {
					match = i.getMacros().iterator().next().getPath();
					access_path = access_path + "-" + i.getMacros().iterator().next().getTo();
					build(path, match, macro, i);
				}
			}

		}

	}

	private void build(Path path, String match, Macro m, Interface i) throws Exception {
		final java.nio.file.PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + match + "}");
		List<String> list = new ArrayList<>();
		Files.walkFileTree(path, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				// TODO Auto-generated method stub
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// if (matcher != null &&
				// matcher.matches(file.getFileName())) {
				list.add(file.toString());
				// }
				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				log.info("Path not found : " + file);
				return FileVisitResult.TERMINATE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				// TODO Auto-generated method stub
				return FileVisitResult.SKIP_SIBLINGS;
			}

		});
		m.setData((Serializable) list);
		register(CacheProcessor.class, m, i);
	}

}

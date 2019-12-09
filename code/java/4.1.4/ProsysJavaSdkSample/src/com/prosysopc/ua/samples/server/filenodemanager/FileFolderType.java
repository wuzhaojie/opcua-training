/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.filenodemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.server.NodeManager;
import com.prosysopc.ua.server.NodeManagerRoot;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.types.opcua.FileDirectoryType;
import com.prosysopc.ua.types.opcua.server.FileDirectoryTypeNode;
import com.prosysopc.ua.types.opcua.server.FileTypeNode;

/**
 * Implementation of a FolderType that contains files, i.e. maps to a Directory on a physical disk.
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/;i=13353")
public class FileFolderType extends FileDirectoryTypeNode {

  private class FileFolderPair {
    public final FileFolderType fileFolderType;
    public final FileTypeNode fileType;

    public FileFolderPair(FileFolderType fft) {
      fileFolderType = fft;
      fileType = null;
    }

    public FileFolderPair(FileTypeNode ft) {
      fileFolderType = null;
      fileType = ft;
    }

  }

  private static Logger logger = LoggerFactory.getLogger(FileFolderType.class);

  /**
   * @param nodeManager
   * @param file
   * @return the BrowseName corresponding to the file. It equals to the name of the file in the
   *         namespace of nodeManager
   */
  public static QualifiedName browseNameForFile(NodeManager nodeManager, File file) {
    return browseNameForFile(nodeManager, file.getName());
  }

  /**
   * @param nodeManager
   * @param fileName
   * @return the BrowseName corresponding to the fileName. It equals to the fileName in the
   *         namespace of nodeManager
   */
  public static QualifiedName browseNameForFile(NodeManager nodeManager, String fileName) {
    return new QualifiedName(nodeManager.getNamespaceIndex(), fileName);
  }

  /**
   * Defines a "standard" mapping between file names and NodeIds, used internally by the manager to
   * create IDs for new files.
   * 
   * @param nodeManager
   * @param file the file
   * @return the nodeId corresponding to the file. The NodeId is composed of the file path in the
   *         namespace of nodeManager
   */
  public static NodeId nodeIdForFile(NodeManager nodeManager, File file) {
    return new NodeId(nodeManager.getNamespaceIndex(), file.getPath());
  }

  private final Set<File> failedFiles = new HashSet<File>();

  private File file;

  private String filter;

  private final Map<File, FileFolderPair> map = new ConcurrentSkipListMap<File, FileFolderPair>();

  private boolean writable;

  protected FileFolderType(NodeManagerUaNode nodeManager, NodeId nodeId, QualifiedName browseName,
      LocalizedText displayName) {
    super(nodeManager, nodeId, browseName, displayName);
    initNodeVersion();
  }

  protected FileFolderType(NodeManagerUaNode nodeManager, NodeId nodeId, String name) {
    this(nodeManager, nodeId, new QualifiedName(nodeManager.getNamespaceIndex(), name), new LocalizedText(name));
    initNodeVersion();
  }

  /**
   * Add the file as a component to the folder. Creates a new {@link FileTypeNode} - or
   * FileFolderType, if the file is a directory.
   * 
   * @param file the file to add
   * @return true if the file was added
   * @throws StatusException with Bad_NotWritable, if the file cannot be written, even though
   *         {@link #isWritable()} is set.
   */
  public boolean addFile(File file) throws StatusException {
    logger.debug("addFile: file={}", file);
    if (file.isDirectory()) {
      return addFolder(file);
    } else if (contains(file)) {
      nodeForFile(file).updateFileSize();
      return false;
    } else {
      NodeId idForFile = nodeIdForFile(file);
      UaNode node = nodeManager.findNode(idForFile);
      if (node == null) {
        FileTypeNode fileType = nodeManager.createInstance(FileTypeNode.class, idForFile, browseNameForFile(file),
            displayNameForFile(file));
        nodeManager.addNode(fileType);
        return addFile(fileType);
      } else if (node instanceof FileTypeNode) {
        return addFile((FileTypeNode) node);
      } else {
        logger.error("Existing node for NodeId {} is not FileTypeNode, node={}", idForFile, node);
        return false;
      }
    }
  }

  /**
   * Add the instance of FileType as a component to the folder.
   * 
   * @param fileTypeNode the node to add
   * @return true if the file was added
   * @throws StatusException with Bad_NotWritable, if the file cannot be written, even though
   *         {@link #isWritable()} is set.
   */
  public boolean addFile(FileTypeNode fileTypeNode) throws StatusException {
    return addFile(fileTypeNode, false);
  }

  /**
   * Add the instance of FileType as a component to the folder.
   * 
   * @param fileTypeNode the node to add
   * @param createFile if true, the file is created if it does not exist (along with the folder path
   *        to the file)
   * @return true if the file was added
   * @throws StatusException with Bad_NotWritable, if the file cannot be written, event though
   *         {@link #isWritable()} is set.
   */
  public boolean addFile(FileTypeNode fileTypeNode, boolean createFile) throws StatusException {
    if (fileTypeNode.getFile() == null) {
      File newFile = new File(getFile(), fileTypeNode.getBrowseName().getName());
      if (createFile && !newFile.exists()) {
        newFile.getParentFile().mkdirs();
        try {
          newFile.createNewFile();
          // try to set last modified to 0 because we can write the
          // file from client and this cannot be newer than the client
          // side file
          newFile.setLastModified(0);
        } catch (IOException e) {
          logger.error("Could not create new file, {}", newFile);
        }
      }
      fileTypeNode.setFile(newFile);
    }
    return doAddFile(fileTypeNode);
  }

  /**
   * Add a new sub folder in this folder
   * 
   * @param folder the folder to add
   * @return true if the folder is added, false if it was already there
   */
  public boolean addFolder(FileFolderType folder) {
    if (!contains(folder)) {

      addReference(folder, Identifiers.Organizes, false);
      folder.initNodeVersion();
      try {
        getNodeManager().addNode(folder);
      } catch (StatusException e) {
        logger.error("Could not add sub-node to nodemanager", e);
      }
      return map.put(folder.getFile(), new FileFolderPair(folder)) != null;
    }
    return false;
  }

  /**
   * @param file
   * @return the BrowseName corresponding to the file. It equals to the name of the file in the
   *         namespace of this nodeManager
   */
  public QualifiedName browseNameForFile(File f) {
    return browseNameForFile(f.getName());
  }

  /**
   * @param fileName
   * @return the BrowseName corresponding to the fileName. It equals to the fileName in the
   *         namespace of this nodeManager
   */
  public QualifiedName browseNameForFile(String fileName) {
    return new QualifiedName(getNodeManager().getNamespaceIndex(), fileName);
  }

  /**
   * Checks if the file is in the folder.
   * 
   * @param file the file
   * @return true if the file is in the folder
   */
  public boolean contains(File file) {
    return map.containsKey(file);
  }

  /**
   * Checks if the folder is in the folder.
   * 
   * @param folder the folder
   * @return true if the file is in the folder
   */
  public boolean contains(FileFolderType folder) {
    return map.containsKey(folder.getFile());
  }

  /**
   * Checks if the file is in the folder.
   * 
   * @param file the FileTypeNode
   * @return true if the file is in the folder
   */
  public boolean contains(FileTypeNode file) {
    return map.containsKey(file.getFile());
  }

  /**
   * @param fileName
   * @return the DisplayName corresponding to the fileName. It equals to the fileName with
   *         LocalizedText.NO_LOCALE
   */
  public LocalizedText displayNameForFile(File f) {
    return new LocalizedText(f.getName(), LocalizedText.NO_LOCALE);
  }

  /**
   * If files cannot be added to monitoring in {@link #refresh()} they will be added to the failed
   * files. A logger warning is also generated for them in the first failure. Remove the file from
   * the list to make the manager retry adding it.
   * 
   * @return the failedFiles
   */
  public Set<File> getFailedFiles() {
    return failedFiles;
  }

  /**
   * @return the file object corresponding to this folder
   */
  public File getFile() {
    return file;
  }

  /**
   * @return the filter
   */
  public String getFilter() {
    return filter;
  }

  /**
   * @return the writable
   */
  public boolean isWritable() {
    return writable;
  }

  /**
   * Checks if the file matches the defined filter.
   * <p>
   * The file name is checked if it contains any of the comma delimited patterns in the filter. Each
   * pattern may begin or start with * as a wildcard, in which case it must match the end or
   * beginning of the pattern exactly. If no wildcards are defined then a match is true, if the file
   * name contains the pattern.
   * 
   * @param file the file whose name is checked
   * @return true if the file matches the filter
   */
  public boolean matchesFilter(File file) {
    return matchesFilter(file.getName());
  }

  /**
   * Checks if the file matches the defined filter.
   * <p>
   * The file name is checked if it contains any of the comma delimited patterns in the filter. Each
   * pattern may begin or start with * as a wildcard, in which case it must match the end or
   * beginning of the pattern exactly. If no wildcards are defined then a match is true, if the file
   * name contains the pattern.
   * 
   * @param fileName
   * @return true if the file matches the filter
   */
  public boolean matchesFilter(String fileName) {
    if ((getFilter() == null) || getFilter().isEmpty()) {
      return true;
    }
    for (String s : getFilter().split("\\s*,\\s*")) {
      String regex = "^" + s.replace("?", ".?").replace("*", ".*?") + "$";
      if (fileName.matches(regex)) {
        return true;
        // MatchType m = MatchType.None;
        // if (s.startsWith("*"))
        // s = s.substring(1);
        // if (s.endsWith("*")) {
        // s = s.substring(0, s.length());
        // return true;
        // }
        // if (file.getName().contains(s))
        // return true;
        // Regexp r = new Regexp(s);
        // if (r)
      }
    }

    return false;
  }

  /**
   * Finds the FileTypeNode corresponding to the file.
   * 
   * @param file the file to look for
   * @return the node or null, if the file is not known to the folder.
   */
  public FileTypeNode nodeForFile(File file) {
    FileFolderPair ffp = map.get(file);
    return ffp == null ? null : ffp.fileType;
  }

  /**
   * Defines a "standard" mapping between file names and NodeIds, used internally by the manager to
   * create IDs for new files.
   * 
   * @param file the file
   * @return the nodeId corresponding to the file. The NodeId is composed of the file path in the
   *         namespace of the manager.
   */
  public NodeId nodeIdForFile(File file) {
    return new NodeId(getNodeManager().getNamespaceIndex(), file.getPath());
  }

  /**
   * Refreshes the nodes of the folder according to the current files in the disk that match the
   * criteria.
   */
  public void refresh() {
    if (logger.isDebugEnabled()) {
      logger.debug("FileFolderType.refresh for folder:" + getFile());
    }
    if (getFile() != null) {
      final NodeManagerRoot rootManager = getNodeManager().getServer().getNodeManagerRoot();
      rootManager.beginModelChange();
      try {
        if (!getFile().exists()) {
          // remove all files because folder does not exist
          Iterator<Entry<File, FileFolderPair>> i = map.entrySet().iterator();
          while (i.hasNext()) {
            Entry<File, FileFolderPair> e = i.next();
            if (e.getValue().fileType != null) {
              removeFile(e.getValue().fileType);
            }
            if (e.getValue().fileFolderType != null) {
              removeFolder(e.getValue().fileFolderType);
            }
            i.remove();
          }
        } else {
          // filter files
          List<File> files = new ArrayList<File>();
          for (File f : getFile().listFiles()) {
            if (matchesFilter(f)) {
              files.add(f);
            }
          }

          // remove old files
          Iterator<Entry<File, FileFolderPair>> i = map.entrySet().iterator();
          while (i.hasNext()) {
            Entry<File, FileFolderPair> e = i.next();
            if (!files.contains(e.getKey())) {
              // file should be removed
              if (e.getValue().fileType != null) {
                removeFile(e.getValue().fileType);
              }
              if (e.getValue().fileFolderType != null) {
                removeFolder(e.getValue().fileFolderType);
              }
              i.remove();
            } else
            // file should be kept, update timestamp if needed
            if (e.getValue().fileType != null) {
              updateFile(e.getValue().fileType);
            }
          }

          // add new files
          for (File f : files) {
            if (!contains(f) && !failedFiles.contains(f)) {
              try {
                addFile(f);
              } catch (StatusException e) {
                failedFiles.add(f);
                logger.warn("Failed to add file in refresh: " + f);
              }
            }
          }

          // update every sub-folder
          for (UaReference r : getReferences(Identifiers.Organizes, false)) {
            if (r.getTargetNode() instanceof FileFolderType) {
              ((FileFolderType) r.getTargetNode()).refresh();
            }
          }
        }
      } finally {
        rootManager.endModelChange();
      }
    }
  }

  /**
   * Removes a node corresponding to a file from the folder, if it is not open.
   * 
   * @param fileTypeNode the node to remove
   */
  public void removeFile(FileTypeNode fileTypeNode) {
    synchronized (fileTypeNode) {
      if (fileTypeNode.getCurrentOpenCount() != 0) {
        return;
      }
      for (UaReference r : getReferences(false)) {
        if (r.getTargetNode().equals(fileTypeNode)) {
          try {
            getNodeManager().deleteNode(r.getTargetNode(), true, true);
          } catch (StatusException e) {
            logger.error("Could not delete node", e);
          }
        }
      }
      map.remove(fileTypeNode.getFile());
    }
  }

  /**
   * Removes a sub folder from this folder.
   * 
   * @param folder the folder to remove
   */
  public void removeFolder(FileFolderType folder) {
    for (UaReference r : getReferences(false)) {
      if (r.getTargetNode().equals(folder)) {
        try {
          getNodeManager().deleteNode(r.getTargetNode(), true, true);
        } catch (StatusException e) {
          logger.error("Could not delete node", e);
        }
      }
    }
    map.remove(folder.getFile());
  }

  /**
   * Define the folder to watch.
   * 
   * @param file the file object corresponding to this folder. The file object is not verified,
   *        whether it is actually a directory or exists.
   */
  public void setFile(File file) {
    this.file = file;
  }

  /**
   * Define the folder to watch as a string path. The path will be converted to a file and used to
   * call {@link #setFile(File)}.
   * 
   * @param path the path to set
   */
  public void setFile(String path) {
    setFile(new File(path));
  }

  /**
   * Define an optional filter which is used to select only files that match the specific criteria.
   * 
   * @param filter the filter to set. The filter may contain a comma separated list of patterns. See
   *        {@link #matchesFilter(File)} for the detailed pattern rules.
   */
  public void setFilter(String filter) {
    this.filter = filter;
    refresh();
  }

  /**
   * @param writable the writable to set
   */
  public void setWritable(boolean writable) {
    this.writable = writable;
  }

  private boolean addFolder(File file) {
    if (logger.isDebugEnabled()) {
      logger.debug("addFolder: file=" + file);
    }
    if (!contains(file)) {
      FileFolderType folder = null;
      NodeId id = nodeIdForFile(file);
      try {
        UaNode node = nodeManager.getNode(id);
        if (node instanceof FileFolderType) {
          folder = (FileFolderType) node;
        } else {
          logger.error("A NodeId exists for the folder " + file + " already and it is not of type FileFolderType");
          return false; // XXX not exactly correct, but cannot do
          // better
        }
      } catch (StatusException e) {
        // does not exist yet
        folder = new FileFolderType(getNodeManager(), id, browseNameForFile(file), displayNameForFile(file));
      }
      folder.setFile(file);
      return addFolder(folder);
    }
    return false;
  }

  private boolean doAddFile(FileTypeNode fileType) throws StatusException {
    if (!contains(fileType)) {
      fileType.setCurrentWritable(isWritable());
      addComponent(fileType);
      return map.put(fileType.getFile(), new FileFolderPair(fileType)) != null;
    }
    return false;
  }

  private void updateFile(FileTypeNode fileType) {
    fileType.updateFileSize();
  }

  @Override
  protected NodeId onCreateDirectory(ServiceContext serviceContext, String directoryName) throws StatusException {
    // TODO implement and handle user authorization
    throw new StatusException(StatusCodes.Bad_NotImplemented);
  }

  @Override
  protected FileDirectoryType.CreateFileMethodOutputs onCreateFile(ServiceContext serviceContext, String fileName,
      Boolean requestFileOpen) throws StatusException {
    // TODO implement and handle user authorization
    throw new StatusException(StatusCodes.Bad_NotImplemented);
  }

  @Override
  protected void onDelete(ServiceContext serviceContext, NodeId objectToDelete) throws StatusException {
    // TODO implement and handle user authorization
    throw new StatusException(StatusCodes.Bad_NotImplemented);
  }

  @Override
  protected NodeId onMoveOrCopy(ServiceContext serviceContext, NodeId objectToMoveOrCopy, NodeId targetDirectory,
      Boolean createCopy, String newName) throws StatusException {
    // TODO implement and handle user authorization
    throw new StatusException(StatusCodes.Bad_NotImplemented);
  }

}

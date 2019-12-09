/**
 * Prosys OPC UA Java SDK
 * Copyright (c) Prosys OPC Ltd.
 * <http://www.prosysopc.com>
 * All rights reserved.
 */
package com.prosysopc.ua.samples.server.filenodemanager;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReferenceType;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.nodes.BaseNode;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.AccessLevelType.Fields;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.NodeAttributes;
import com.prosysopc.ua.stack.core.NodeClass;
import com.prosysopc.ua.types.opcua.FolderType;
import com.prosysopc.ua.types.opcua.server.FileTypeNode;

/**
 * OPC UA Node Manager, which is specialized in managing files.
 * <p>
 * It extends the NodeManagerUaNode, and additionally, it can watch for file objects in the disk and
 * notify the clients about changes in them. You can declare folders to watch and also manually add
 * files of FileType to the manager.
 * <p>
 * It will also manage node management requests from client applications to create or remove files,
 * if allowed to do so.
 */
public class FileNodeManager extends NodeManagerUaNode {

  private static final int FILES_FOLDER_ID = 1;
  private static Logger logger = LoggerFactory.getLogger(FileNodeManager.class);
  private boolean allowWrite = false;
  private final List<FileFolderType> folders = new CopyOnWriteArrayList<FileFolderType>();
  private long monitoringInterval = 1000;

  private TimerTask monitorTask;
  private Timer monitorTimer;
  private final FolderType rootFolder;

  /**
   * Create a new FileNodeManager that should manage files.
   *
   * @param server The server in which the manager is added
   * @param namespaceUri The Namespace URI that this manager will manage: all nodes will be in this
   *        namespace
   * @param rootFolderName The name of the RootFolder under which all sub folders will be added. Use
   *        null or empty to use the manager without any root folder. In that case, you will need to
   *        add all folders manually under some other node.
   */
  public FileNodeManager(UaServer server, String namespaceUri, String rootFolderName) {
    super(server, namespaceUri);
    if ((rootFolderName != null) && !rootFolderName.isEmpty()) {
      rootFolder = createInstance(FolderType.class, rootFolderName, new NodeId(getNamespaceIndex(), FILES_FOLDER_ID));
    } else {
      rootFolder = null;
    }
    getNodeFactory().registerTypeDefinition(Identifiers.FileDirectoryType, FileFolderType.class);
  }

  /**
   * Adds a new physical directory to the node manager.
   * <p>
   * Creates a new FileFolderType node with 'folder' and adds it to the node manager. If
   * {@link #getRootFolder()} is defined, the folder will be added under it, using an Organizes
   * reference.
   *
   * @param folder the File corresponding to the folder.
   * @return the created node that is mapping the folder to the server
   * @throws StatusException if the new node cannot be added to the node manager.
   */
  public FileFolderType addFolder(File folder) throws StatusException {
    logger.debug("addFolder: dir={}", folder);
    FileFolderType folderType = (FileFolderType) createInstance(Identifiers.FileDirectoryType, nodeIdForFile(folder),
        new QualifiedName(folder.getName()), new LocalizedText(folder.getName()));

    folderType.setFile(folder);
    // folderType.initNodeVersion(); this is done in FileFolderType
    // constructor
    addNode(folderType); // trigger model change
    folders.add(folderType);
    if (rootFolder != null) {
      rootFolder.addReference(folderType, Identifiers.Organizes, false);
    }
    try {
      folderType.refresh();
    } catch (Exception e) {
      logger.error("Could not refresh folder " + folder, e);
    }
    startMonitoring();
    return folderType;
  }

  /**
   * Adds a new physical directory to the node manager.
   * <p>
   * Creates a new FileFolderType node with 'folder' and adds it to the node manager. If
   * {@link #getRootFolder()} is defined, the folder will be added under it, using an Organizes
   * reference.
   *
   * @param folderName the name of the folder to map
   * @return the created node that is mapping the folder to the server
   * @throws StatusException if the new node cannot be added to the node manager.
   */
  public FileFolderType addFolder(String folderName) throws StatusException {
    return addFolder(new File(folderName));
  }

  /*
   * (non-Javadoc)
   *
   * @see com.prosysopc.ua.server.NodeManagerUaNode#addNode(com.prosysopc.ua.nodes .UaNode)
   */
  @Override
  public UaNode addNode(UaNode newNode) throws StatusException {
    UaNode node = super.addNode(newNode);
    if (allowWrite && (node instanceof FileTypeNode)) {
      BaseNode.setAccessLevelRecursively(node,
          AccessLevelType.of(AccessLevelType.Fields.CurrentRead, AccessLevelType.Fields.CurrentWrite),
          Identifiers.HasComponent, Identifiers.HasProperty);
      ((FileTypeNode) node).setWritable(Boolean.TRUE);
    }
    if (node instanceof FileTypeNode) {
      AccessLevelType s = ((FileTypeNode) node).getSizeNode().getAccessLevel();
      Set<Fields> ns = s.toSet();
      ns.add(AccessLevelType.Fields.SemanticChange);
      ((FileTypeNode) node).getSizeNode().setAccessLevel(AccessLevelType.of(ns));
    }

    return node;
  }

  /**
   * @param file
   * @return the BrowseName corresponding to the fileName. It equals to the name of the file in the
   *         namespace of this nodeManager
   */
  public QualifiedName browseNameForFile(File file) {
    return FileFolderType.browseNameForFile(this, file);
  }

  /**
   * @param fileName
   * @return the BrowseName corresponding to the fileName. It equals to the fileName in the
   *         namespace of this nodeManager
   */
  public QualifiedName browseNameForFile(String fileName) {
    return FileFolderType.browseNameForFile(this, fileName);
  }

  /**
   * The monitoring interval, i.e. how often files are checked for modifications.
   *
   * @return the monitoringInterval in milliseconds
   */
  public long getMonitoringInterval() {
    return monitoringInterval;
  }

  /**
   * The folders will be organized under a root folder of the NodeManager.
   *
   * @return the rootFolder
   */
  public FolderType getRootFolder() {
    return rootFolder;
  }

  /**
   * If allowWrite is true, then all new files added to this managers address space are writable
   * (default=false).
   *
   * @return allowWrite
   */
  public boolean isAllowWrite() {
    return allowWrite;
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
    return FileFolderType.nodeIdForFile(this, file);
  }

  /**
   * Refresh all folders to update the node structure in the address space.
   *
   * @see #setMonitoringInterval(long)
   */
  public void refresh() {
    logger.debug("FileNodeManager.refresh");
    for (FileFolderType f : folders) {
      try {
        f.refresh();
      } catch (Exception e) {
        logger.error("Could not refresh folder " + f.getFile(), e);
      }
    }
  }

  /**
   * If allowWrite is true, all new files added to this managers address space are writable
   * (default=false).
   *
   * @param allowWrite
   */
  public void setAllowWrite(boolean allowWrite) {
    this.allowWrite = allowWrite;
  }

  /**
   * Define the interval of file monitoring. The manager will refresh the folders that it's watching
   * with an internal timer task.
   *
   * @param monitoringInterval the monitoringInterval to set in milliseconds. Default is 1000 ms
   */
  public void setMonitoringInterval(long monitoringInterval) {
    this.monitoringInterval = monitoringInterval;
    startMonitoring();
  }

  /**
   * Define the interval of file monitoring. The manager will refresh the folders that it's watching
   * with an internal timer task.
   *
   * @param monitoringInterval the monitoringInterval to set in the defined timeUnit. Default is
   *        1000 ms
   * @param timeUnit the unit in which the monitoringInterval is defined
   */
  public void setMonitoringInterval(long monitoringInterval, TimeUnit timeUnit) {
    setMonitoringInterval(timeUnit.toMillis(monitoringInterval));
  }

  /**
   * Start file monitoring according to {@link #getMonitoringInterval()}.
   *
   * @see #stopMonitoring()
   */
  public void startMonitoring() {
    if (isStarted() && (monitorTimer == null) && (monitoringInterval != 0)) {
      logger.debug("startMonitor");
      monitorTask = new TimerTask() {

        @Override
        public void run() {
          refresh();
        }
      };
      monitorTimer = new Timer();
      monitorTimer.scheduleAtFixedRate(monitorTask, 0, monitoringInterval);;

    }
  };

  /**
   * Stop monitoring files.
   */
  public void stopMonitoring() {
    if (monitorTimer != null) {
      monitorTimer.cancel();
      monitorTimer = null;
      monitorTask = null;
    }
  }

  @Override
  protected void close() {
    stopMonitoring();
    super.close();
  }

  @Override
  protected void fireAfterAddNode(ServiceContext serviceContext, NodeId parentNodeId, UaNode parent, NodeId nodeId,
      UaNode node, NodeClass nodeClass, QualifiedName browseName, NodeAttributes attributes,
      UaReferenceType referenceType, ExpandedNodeId typeDefinitionId, UaNode typeDefinition) throws StatusException {
    super.fireAfterAddNode(serviceContext, parentNodeId, parent, nodeId, node, nodeClass, browseName, attributes,
        referenceType, typeDefinitionId, typeDefinition);
    if ((node instanceof FileTypeNode) && (parent instanceof FileFolderType)) {
      FileTypeNode newFileType = (FileTypeNode) node;
      FileFolderType folder = (FileFolderType) parent;
      folder.addFile(newFileType, true); // must create the file now
    }

    if ((node instanceof FileFolderType) && (parent instanceof FileFolderType)) {
      FileFolderType p = (FileFolderType) parent;
      FileFolderType newFolder = (FileFolderType) node;
      File newFolderFile = new File(p.getFile(), newFolder.getBrowseName().getName());
      newFolderFile.mkdirs();// must create right away
      newFolder.setFile(newFolderFile);
      p.addFolder(newFolder);
    }
  }

  @Override
  protected void start() throws StatusException, com.prosysopc.ua.nodes.UaNodeFactoryException {
    super.start();
    startMonitoring();
  }
}

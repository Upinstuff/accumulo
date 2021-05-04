/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.accumulo.core.metadata.schema;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.accumulo.core.metadata.StoredTabletFile;
import org.apache.accumulo.core.metadata.TabletFile;
import org.apache.accumulo.core.spi.compaction.CompactionExecutorId;
import org.apache.accumulo.core.spi.compaction.CompactionKind;
import org.apache.hadoop.fs.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExternalCompactionMetadata {

  private static final Gson GSON = new GsonBuilder().create();

  private final Set<StoredTabletFile> jobFiles;
  private final Set<StoredTabletFile> nextFiles;
  private final TabletFile compactTmpName;
  private final TabletFile newFile;
  private final String compactorId;
  private final CompactionKind kind;
  private final long priority;
  private final CompactionExecutorId ceid;
  private final boolean propogateDeletes;
  private final boolean selectedAll;
  private final Long compactionId;

  public ExternalCompactionMetadata(Set<StoredTabletFile> jobFiles, Set<StoredTabletFile> nextFiles,
      TabletFile compactTmpName, TabletFile newFile, String compactorId, CompactionKind kind,
      long priority, CompactionExecutorId ceid, boolean propogateDeletes, boolean selectedAll,
      Long compactionId) {
    this.jobFiles = Objects.requireNonNull(jobFiles);
    this.nextFiles = Objects.requireNonNull(nextFiles);
    this.compactTmpName = Objects.requireNonNull(compactTmpName);
    this.newFile = Objects.requireNonNull(newFile);
    this.compactorId = Objects.requireNonNull(compactorId);
    this.kind = Objects.requireNonNull(kind);
    this.priority = priority;
    this.ceid = Objects.requireNonNull(ceid);
    this.propogateDeletes = propogateDeletes;
    this.selectedAll = selectedAll;
    this.compactionId = compactionId;
  }

  public Set<StoredTabletFile> getJobFiles() {
    return jobFiles;
  }

  public Set<StoredTabletFile> getNextFiles() {
    return nextFiles;
  }

  public TabletFile getCompactTmpName() {
    return compactTmpName;
  }

  public TabletFile getNewFile() {
    return newFile;
  }

  public String getCompactorId() {
    return compactorId;
  }

  public CompactionKind getKind() {
    return kind;
  }

  public long getPriority() {
    return priority;
  }

  public CompactionExecutorId getCompactionExecutorId() {
    return ceid;
  }

  public boolean isPropogateDeletes() {
    return propogateDeletes;
  }

  public boolean isSelectedAll() {
    return selectedAll;
  }

  public Long getCompactionId() {
    return compactionId;
  }

  // This class is used to serialize and deserialize this class using GSon. Any changes to this
  // class must consider persisted data.
  private static class GSonData {
    List<String> inputs;
    List<String> nextFiles;
    String tmp;
    String dest;
    String compactor;
    String kind;
    String executorId;
    long priority;
    boolean propDels;
    boolean selectedAll;
    Long compactionId;
  }

  public String toJson() {
    GSonData jData = new GSonData();

    jData.inputs = jobFiles.stream().map(StoredTabletFile::getMetaUpdateDelete).collect(toList());
    jData.nextFiles =
        nextFiles.stream().map(StoredTabletFile::getMetaUpdateDelete).collect(toList());
    jData.tmp = compactTmpName.getMetaInsert();
    jData.dest = newFile.getMetaInsert();
    jData.compactor = compactorId;
    jData.kind = kind.name();
    jData.executorId = ceid.getExernalName();
    jData.priority = priority;
    jData.propDels = propogateDeletes;
    jData.selectedAll = selectedAll;
    jData.compactionId = compactionId;
    return GSON.toJson(jData);
  }

  public static ExternalCompactionMetadata fromJson(String json) {
    GSonData jData = GSON.fromJson(json, GSonData.class);

    return new ExternalCompactionMetadata(
        jData.inputs.stream().map(StoredTabletFile::new).collect(toSet()),
        jData.nextFiles.stream().map(StoredTabletFile::new).collect(toSet()),
        new TabletFile(new Path(jData.tmp)), new TabletFile(new Path(jData.dest)), jData.compactor,
        CompactionKind.valueOf(jData.kind), jData.priority,
        CompactionExecutorId.externalId(jData.executorId), jData.propDels, jData.selectedAll,
        jData.compactionId);
  }

  @Override
  public String toString() {
    return toJson();
  }
}

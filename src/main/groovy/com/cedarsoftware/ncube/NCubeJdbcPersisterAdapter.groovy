package com.cedarsoftware.ncube

import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Connection
/**
 * This adapter could be replaced by an adapting proxy.  Then you could
 * implement the interface and the class and not need this class.
 *
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License")
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         http://www.apache.org/licenses/LICENSE-2.0
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
@CompileStatic
class NCubeJdbcPersisterAdapter implements NCubePersister
{
    private final NCubeJdbcPersister persister = new NCubeJdbcPersister()
    private final JdbcConnectionProvider connectionProvider
    private static final Logger LOG = LoggerFactory.getLogger(NCubeJdbcPersisterAdapter.class)

    NCubeJdbcPersisterAdapter(JdbcConnectionProvider provider)
    {
        connectionProvider = provider
    }

    Object jdbcOperation(Closure closure, String msg, String username = 'no user set')
    {
        Connection c = connectionProvider.connection
        try
        {
            long start = System.nanoTime()
            Object ret = closure(c)
            long end = System.nanoTime()
            long time = Math.round((end - start) / 1000000.0d)
            if (time > 1000)
            {
                LOG.info("    [SLOW DB - ${time} ms] [${username}] ${msg}")
            }
            return ret
        }
        finally
        {
            connectionProvider.releaseConnection(c)
        }
    }

    void updateCube(NCube cube, final String username)
    {
        jdbcOperation({ Connection c -> persister.updateCube(c, cube, username) },
                "updateCube(${cube.applicationID.cacheKey(cube.name)})", username)
    }
    void createCube(NCube cube, final String username)
    {
        jdbcOperation({ Connection c -> persister.createCube(c, cube, username) },
                "createCube(${cube.applicationID.cacheKey(cube.name)})", username)
    }

    String loadCubeRawJson(ApplicationID appId, String name, String username)
    {
        return (String) jdbcOperation({ Connection c -> persister.loadCubeRawJson(c, appId, name) },
                "loadCubeRawJson(${appId.cacheKey(name)})", username)
    }

    NCube loadCubeById(long cubeId, String username)
    {
        return (NCube) jdbcOperation({ Connection c -> persister.loadCubeById(c, cubeId) },
                "loadCubeById(${cubeId})", username)
    }

    NCube loadCube(ApplicationID appId, String name, String username)
    {
        return (NCube) jdbcOperation({ Connection c -> persister.loadCube(c, appId, name) },
                "loadCube(${appId.cacheKey(name)})", username)
    }

    NCube loadCubeBySha1(ApplicationID appId, String name, String sha1, String username)
    {
        return (NCube) jdbcOperation({ Connection c -> persister.loadCubeBySha1(c, appId, name, sha1) },
                "loadCubeBySha1(${appId.cacheKey(name)}, ${sha1})", username)
    }

    boolean restoreCubes(ApplicationID appId, Object[] names, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.restoreCubes(c, appId, names, username) },
                "restoreCubes(${appId})", username)
    }

    List<NCubeInfoDto> getRevisions(ApplicationID appId, String cubeName, boolean ignoreVersion, String username)
    {
        return (List<NCubeInfoDto>) jdbcOperation({ Connection c -> persister.getRevisions(c, appId, cubeName, ignoreVersion) },
                "getRevisions(${appId.cacheKey(cubeName)}", username)
    }

    Set<String> getBranches(ApplicationID appId, String username)
    {
        return (Set<String>) jdbcOperation({ Connection c -> persister.getBranches(c, appId) },
                "getBranches(${appId})", username)
    }

    boolean deleteBranch(ApplicationID branchId, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.deleteBranch(c, branchId) },
                "deleteBranch(${branchId})", username)
    }

    boolean doCubesExist(ApplicationID appId, boolean ignoreStatus, String methodName, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.doCubesExist(c, appId, ignoreStatus, methodName) },
                "doCubesExist(${appId})", username)
    }

    boolean deleteCubes(ApplicationID appId, Object[] cubeNames, boolean allowDelete, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.deleteCubes(c, appId, cubeNames, allowDelete, username) },
                "deleteCubes(${appId})", username)
    }

    List<String> getAppNames(String tenant, String username)
    {
        return (List<String>) jdbcOperation({ Connection c -> persister.getAppNames(c, tenant) },
                "getAppNames(${tenant})", username)
    }

    Map<String, List<String>> getVersions(String tenant, String app, String username)
    {
        return (Map<String, List<String>>) jdbcOperation({ Connection c -> persister.getVersions(c, tenant, app) },
                "getVersions(${tenant}, ${app})", username)
    }

    int changeVersionValue(ApplicationID appId, String newVersion, String username)
    {
        return (int) jdbcOperation({ Connection c -> persister.changeVersionValue(c, appId, newVersion) },
                "changeVersionValue(${appId}, ${newVersion})", username)
    }

    int moveBranch(ApplicationID appId, String newSnapVer, String username)
    {
        return (int) jdbcOperation({ Connection c -> persister.moveBranch(c, appId, newSnapVer) },
                "moveBranch(${appId}, to version ${newSnapVer})", username)
    }

    int releaseCubes(ApplicationID appId, String newSnapVer, String username)
    {
        return (int) jdbcOperation({ Connection c -> persister.releaseCubes(c, appId, newSnapVer) },
                "releaseCubes(${appId}, new snap ver: ${newSnapVer})", username)
    }

    int copyBranch(ApplicationID srcAppId, ApplicationID targetAppId, String username)
    {
        return (int) jdbcOperation({ Connection c -> persister.copyBranch(c, srcAppId, targetAppId) },
                "copyBranch(${srcAppId} -> ${targetAppId})", username)
    }

    int copyBranchWithHistory(ApplicationID srcAppId, ApplicationID targetAppId, String username)
    {
        return (int) jdbcOperation({ Connection c -> persister.copyBranchWithHistory(c, srcAppId, targetAppId) },
                "copyBranchWithHistory(${srcAppId} -> ${targetAppId})", username)
    }

    boolean renameCube(ApplicationID appId, String oldName, String newName, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.renameCube(c, appId, oldName, newName, username) },
                "renameCube(${appId.cacheKey(oldName)} to ${newName})", username)
    }

    boolean mergeAcceptTheirs(ApplicationID appId, String cubeName, String sourceBranch, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.mergeAcceptTheirs(c, appId, cubeName, sourceBranch, username) },
                "mergeAcceptTheirs(${appId.cacheKey(cubeName)}, theirs: ${sourceBranch})", username)
    }

    boolean mergeAcceptMine(ApplicationID appId, String cubeName, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.mergeAcceptMine(c, appId, cubeName, username) },
                "mergeAcceptMine(${appId.cacheKey(cubeName)})", username)
    }

    boolean duplicateCube(ApplicationID oldAppId, ApplicationID newAppId, String oldName, String newName, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.duplicateCube(c, oldAppId, newAppId, oldName, newName, username) },
                "duplicateCube(${oldAppId.cacheKey(oldName)} -> ${newAppId.cacheKey(newName)})", username)
    }

    boolean updateNotes(ApplicationID appId, String cubeName, String notes, String username)
    {
        String ellipse = notes?.length() > 100 ? '...' : ''
        return (boolean) jdbcOperation({ Connection c -> persister.updateNotes(c, appId, cubeName, notes) },
                "updateNotes(${appId.cacheKey(cubeName)}, '${notes?.take(100) + ellipse}')", username)
    }

    boolean updateTestData(ApplicationID appId, String cubeName, String testData, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.updateTestData(c, appId, cubeName, testData) },
                "saveTests(${appId.cacheKey(cubeName)})", username)
    }

    Map getAppTestData(ApplicationID appId, String username)
    {
        return (Map) jdbcOperation({ Connection c -> persister.getAppTestData(c, appId) },
                "getAppTestData(${appId}", username)
    }

    String getTestData(ApplicationID appId, String cubeName, String username)
    {
        return (String) jdbcOperation({ Connection c -> persister.getTestData(c, appId, cubeName) },
                "getTestData(${appId.cacheKey(cubeName)})", username)
    }

    NCubeInfoDto commitMergedCubeToHead(ApplicationID appId, NCube cube, String username, long txId)
    {
        return (NCubeInfoDto) jdbcOperation({ Connection c -> persister.commitMergedCubeToHead(c, appId, cube, username, txId) },
                "commitMergedCubeToHead(${appId.cacheKey(cube.name)}, txID=${txId})", username)
    }

    NCubeInfoDto commitMergedCubeToBranch(ApplicationID appId, NCube cube, String headSha1, String username, long txId)
    {
        return (NCubeInfoDto) jdbcOperation({ Connection c -> persister.commitMergedCubeToBranch(c, appId, cube, headSha1, username, txId) },
                "commitMergedCubeToBranch(${appId.cacheKey(cube.name)}, headSHA1=${headSha1}, txID=${txId})", username)
    }

    List<NCubeInfoDto> commitCubes(ApplicationID appId, Object[] cubeIds, String username, String requestUser, long txId)
    {
        return (List<NCubeInfoDto>) jdbcOperation({ Connection c -> persister.commitCubes(c, appId, cubeIds, username, requestUser, txId) },
                "commitCubes(${appId}, txId=${txId})", username)
    }

    int rollbackCubes(ApplicationID appId, Object[] names, String username)
    {
        return (int) jdbcOperation({ Connection c -> persister.rollbackCubes(c, appId, names, username) },
                "rollbackCubes(${appId}, ${names.length} cubes)", username)
    }

    List<NCubeInfoDto> pullToBranch(ApplicationID appId, Object[] cubeIds, String username, long txId)
    {
        return (List<NCubeInfoDto>) jdbcOperation({ Connection c -> persister.pullToBranch(c, appId, cubeIds, username, txId) },
                "pullToBranch(${appId}, ${cubeIds.length} cubes, txID=${txId})", username)
    }

    boolean updateBranchCubeHeadSha1(Long cubeId, String branchSha1, String headSha1, String username)
    {
        return (boolean) jdbcOperation({ Connection c -> persister.updateBranchCubeHeadSha1(c, cubeId, branchSha1, headSha1) },
                "updateBranchCubeHeadSha1(${cubeId}, ${branchSha1}, ${headSha1})", username)
    }

    List<NCubeInfoDto> search(ApplicationID appId, String cubeNamePattern, String searchValue, Map options, String username)
    {
        return (List<NCubeInfoDto>) jdbcOperation({ Connection c -> persister.search(c, appId, cubeNamePattern, searchValue, options) },
                "search(${appId}, ${cubeNamePattern}, ${searchValue})", username)
    }

    List<NCube> cubeSearch(ApplicationID appId, String cubeNamePattern, String searchValue, Map options, String username)
    {
        return (List<NCube>) jdbcOperation({ Connection c -> persister.cubeSearch(c, appId, cubeNamePattern, searchValue, options) },
                "cubeSearch(${appId}, ${cubeNamePattern}, ${searchValue})", username)
    }

    void clearTestDatabase(String username)
    {
        jdbcOperation({ Connection c -> persister.clearTestDatabase(c) },
                "clearTestDatabase()", username)
    }
}

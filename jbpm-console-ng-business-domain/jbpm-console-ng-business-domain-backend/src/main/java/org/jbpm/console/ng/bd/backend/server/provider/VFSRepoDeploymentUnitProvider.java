/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.bd.backend.server.provider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jbpm.console.ng.bd.service.DeploymentUnitProvider;
import org.jbpm.kie.services.api.DeploymentUnit;
import org.jbpm.kie.services.api.Vfs;
import org.jbpm.kie.services.impl.VFSDeploymentUnit;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.DirectoryStream;
import org.kie.commons.java.nio.file.Path;
import org.uberfire.backend.repositories.Repository;
import org.uberfire.backend.repositories.RepositoryService;

@ApplicationScoped
@Vfs
public class VFSRepoDeploymentUnitProvider implements DeploymentUnitProvider<DeploymentUnit> {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private RepositoryService repositoryService;

    @Override
    public Set<DeploymentUnit> getDeploymentUnits() {
        Set<DeploymentUnit> deploymentUnits = new HashSet<DeploymentUnit>();

        Collection<Repository> repositories = repositoryService.getRepositories();

        if (repositories == null || repositories.isEmpty()) {
            return deploymentUnits;
        }

        for (Repository repository : repositories) {
            Iterable<Path> assetDirectories = ioService.newDirectoryStream(ioService.get(repository.getUri() + "/processes"),
                    new DirectoryStream.Filter<Path>() {
                        @Override
                        public boolean accept(final Path entry) {
                            if (org.kie.commons.java.nio.file.Files.isDirectory(entry)) {
                                return true;
                            }
                            return false;
                        }
                    });

            for (Path p : assetDirectories) {
                String folder = p.toString();
                if (folder.startsWith("/")) {
                    folder = folder.substring(1);
                }
                deploymentUnits.add(new VFSDeploymentUnit(p.getFileName().toString(), repository.getAlias(), folder));
            }
        }
        return deploymentUnits;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

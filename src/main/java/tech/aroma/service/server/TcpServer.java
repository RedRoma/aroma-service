 /*
  * Copyright 2015 Aroma Tech.
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

package tech.aroma.service.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import java.net.SocketException;
import javax.inject.Singleton;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.cassandra.ModuleCassandraDataRepositories;
import tech.aroma.data.cassandra.ModuleCassandraDevCluster;
import tech.aroma.service.ModuleAromaService;
import tech.aroma.service.operations.encryption.ModuleEncryptionMaterialsDev;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.email.service.EmailService;
import tech.aroma.thrift.service.AromaService;
import tech.aroma.thrift.service.AromaServiceConstants;
import tech.aroma.thrift.services.Clients;
import tech.aroma.thrift.services.NoOpEmailService;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This Main Class runs the Aroma Service on a Server Socket.
 *
 * @author SirWellington
 */
@Internal
public final class TcpServer
{
    
    private final static Logger LOG = LoggerFactory.getLogger(TcpServer.class);
    private static final int PORT = AromaServiceConstants.SERVICE_PORT;
    
    public static void main(String[] args) throws TTransportException, SocketException
    {
        Injector injector = Guice.createInjector(new ModuleAromaService(),
                                                 new ModuleCassandraDataRepositories(),
                                                 new ModuleCassandraDevCluster(),
                                                 new ModuleEncryptionMaterialsDev(),
                                                 new RestOfDependencies());
        
        AromaService.Iface aromaService = injector.getInstance(AromaService.Iface.class);
        AromaService.Processor processor = new AromaService.Processor<>(aromaService);
        
        TServerSocket socket = new TServerSocket(PORT);
        socket.getServerSocket().setSoTimeout((int) SECONDS.toMillis(30));
        
        TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(socket)
            .protocolFactory(new TBinaryProtocol.Factory())
            .processor(processor)
            .requestTimeout(60)
            .requestTimeoutUnit(SECONDS)
            .minWorkerThreads(5)
            .maxWorkerThreads(100);
        
        LOG.info("Starting Aroma Service at port {}", PORT);
        
        TThreadPoolServer server = new TThreadPoolServer(serverArgs);
        server.serve();
        server.stop();
    }
    
    private static class RestOfDependencies extends AbstractModule
    {

        @Override
        protected void configure()
        {
            bind(EmailService.Iface.class).toInstance(NoOpEmailService.newInstance());
        }

        @Singleton
        @Provides
        AuthenticationService.Iface provideAuthenticationService()
        {
            try
            {
                return Clients.newPerRequestAuthenticationServiceClient();
            }
            catch (Exception ex)
            {
                LOG.error("Failed to create Authentication Service", ex);
                throw new RuntimeException(ex);
            }
        }
   
    }
    
}

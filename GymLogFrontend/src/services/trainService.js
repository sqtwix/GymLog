// src/services/trainService.js
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

// ЕДИНСТВЕННЫЙ ИСТОЧНИК ПРАВДЫ — только этот интерцептор
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const trainAPI = {
  getTrains: async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('getTrains: нет токе
ivan2@INBOOK_Y2_PLUS C:\Users\ivan2\GymLog
$ docker-compose up --build
[+] Building 79.5s (35/35) FINISHED
 => [internal] load local bake definitions                                 0.1s  => => reading from stdin 985B                                             0.1s  => [backend internal] load build definition from Dockerfile               0.1s  => => transferring dockerfile: 934B                                       0.1s  => [frontend internal] load build definition from Dockerfile              0.1s  => => transferring dockerfile: 470B                                       0.1s  => [frontend internal] load metadata for docker.io/library/nginx:alpine   5.4s  => [frontend internal] load metadata for docker.io/library/node:18        5.2s  => [backend internal] load metadata for mcr.microsoft.com/dotnet/aspnet:  0.6s  => [backend internal] load metadata for mcr.microsoft.com/dotnet/sdk:9.0  0.6s  => [backend internal] load .dockerignore                                  0.0s  => => transferring context: 2B                                            0.0s  => [backend build 1/8] FROM mcr.microsoft.com/dotnet/sdk:9.0@sha256:6c77  0.1s  => => resolve mcr.microsoft.com/dotnet/sdk:9.0@sha256:6c7713b7d107f74301  0.0s  => [backend internal] load build context                                  0.1s  => => transferring context: 25.43kB                                       0.1s  => [backend stage-1 1/3] FROM mcr.microsoft.com/dotnet/aspnet:9.0@sha256  0.0s  => => resolve mcr.microsoft.com/dotnet/aspnet:9.0@sha256:5df8845a131ee13  0.0s  => CACHED [backend stage-1 2/3] WORKDIR /app                              0.0s  => CACHED [backend build 2/8] WORKDIR /app                                0.0s  => CACHED [backend build 3/8] RUN dotnet nuget locals all --clear         0.0s  => CACHED [backend build 4/8] COPY GymLogServer/*.csproj ./GymLogServer/  0.0s  => CACHED [backend build 5/8] RUN dotnet restore ./GymLogServer/GymLogSe  0.0s  => CACHED [backend build 6/8] COPY . .                                    0.0s  => CACHED [backend build 7/8] RUN dotnet restore ./GymLogServer/GymLogSe  0.0s  => CACHED [backend build 8/8] RUN dotnet publish ./GymLogServer/GymLogSe  0.0s  => CACHED [backend stage-1 3/3] COPY --from=build /app/out ./             0.0s  => [backend] exporting to image                                           0.2s  => => exporting layers                                                    0.0s  => => exporting manifest sha256:3fd0964ccdde761c9fcea0e6394b68f9c756e2d3  0.0s  => => exporting config sha256:f627cb903583fdaba4e95b19eb0d105f4bd0cb4d54  0.0s  => => exporting attestation manifest sha256:fd5a380b867b4524b1d8210076e0  0.0s  => => exporting manifest list sha256:59f859ba75b13c7e848cbf6c1756e9f9083  0.0s  => => naming to docker.io/library/gymlog-backend:latest                   0.0s  => => unpacking to docker.io/library/gymlog-backend:latest                0.0s  => [backend] resolving provenance for metadata file                       0.1s  => [frontend internal] load .dockerignore                                 0.0s  => => transferring context: 2B                                            0.0s  => [frontend build 1/6] FROM docker.io/library/node:18@sha256:c6ae79e384  0.1s  => => resolve docker.io/library/node:18@sha256:c6ae79e38498325db67193d39  0.1s  => [frontend internal] load build context                                 6.7s  => => transferring context: 4.03MB                                        6.4s  => CACHED [frontend stage-1 1/3] FROM docker.io/library/nginx:alpine@sha  0.1s  => => resolve docker.io/library/nginx:alpine@sha256:052b75ab72f690f33deb  0.1s  => CACHED [frontend build 2/6] WORKDIR /app                               0.0s  => CACHED [frontend build 3/6] COPY package*.json ./                      0.0s  => CACHED [frontend build 4/6] RUN npm install                            0.0s  => [frontend build 5/6] COPY . .                                         31.1s  => [frontend build 6/6] RUN npm run build                                31.8s  => [frontend stage-1 2/3] COPY --from=build /app/build /usr/share/nginx/  0.1s  => [frontend stage-1 3/3] COPY nginx.conf /etc/nginx/conf.d/default.conf  0.1s  => [frontend] exporting to image                                          0.6s  => => exporting layers                                                    0.3s  => => exporting manifest sha256:7352dec466fbeb3c8e25e6b6e5d09bf6d902f42a  0.0s  => => exporting config sha256:dc22538bf55064367065d34c1b43481505b66eb854  0.0s  => => exporting attestation manifest sha256:604714f75094c3e16e11fca74276  0.0s  => => exporting manifest list sha256:4b5a2fa8e9e007d629d4eede6c38b426292  0.0s  => => naming to docker.io/library/gymlog-frontend:latest                  0.0s  => => unpacking to docker.io/library/gymlog-frontend:latest               0.2s  => [frontend] resolving provenance for metadata file                      0.1s [+] Running 7/7
 ✔ gymlog-frontend              Built                                      0.0s  ✔ gymlog-backend               Built                                      0.0s  ✔ Network gymlog_default       Created                                    0.1s  ✔ Volume gymlog_postgres_data  Created                                    0.0s  ✔ Container gymlog-postgres-1  Created                                    0.4s  ✔ Container gymlog-backend-1   Created                                    0.1s  ✔ Container gymlog-frontend-1  Created                                    0.1s Attaching to backend-1, frontend-1, postgres-1
postgres-1  | The files belonging to this database system will be owned by user "postgres".
postgres-1  | This user must also own the server process.
postgres-1  |
postgres-1  | The database cluster will be initialized with locale "en_US.utf8".postgres-1  | The default database encoding has accordingly been set to "UTF8". postgres-1  | The default text search configuration will be set to "english".
postgres-1  |
postgres-1  | Data page checksums are disabled.
postgres-1  |
postgres-1  | fixing permissions on existing directory /var/lib/postgresql/data ... ok
postgres-1  | creating subdirectories ... ok
postgres-1  | selecting dynamic shared memory implementation ... posix
postgres-1  | selecting default max_connections ... 100
postgres-1  | selecting default shared_buffers ... 128MB
postgres-1  | selecting default time zone ... UTC
postgres-1  | creating configuration files ... ok
frontend-1  | /docker-entrypoint.sh: /docker-entrypoint.d/ is not empty, will attempt to perform configuration
frontend-1  | /docker-entrypoint.sh: Looking for shell scripts in /docker-entrypoint.d/
frontend-1  | /docker-entrypoint.sh: Launching /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh
frontend-1  | 10-listen-on-ipv6-by-default.sh: info: Getting the checksum of /etc/nginx/conf.d/default.conf
postgres-1  | running bootstrap script ... ok
frontend-1  | 10-listen-on-ipv6-by-default.sh: info: /etc/nginx/conf.d/default.conf differs from the packaged version
frontend-1  | /docker-entrypoint.sh: Sourcing /docker-entrypoint.d/15-local-resolvers.envsh
frontend-1  | /docker-entrypoint.sh: Launching /docker-entrypoint.d/20-envsubst-on-templates.sh
frontend-1  | /docker-entrypoint.sh: Launching /docker-entrypoint.d/30-tune-worker-processes.sh
frontend-1  | /docker-entrypoint.sh: Configuration complete; ready for start up frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: using the "epoll" event method
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: nginx/1.29.4
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: built by gcc 15.2.0 (Alpine 15.2.0)
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: OS: Linux 6.6.87.2-microsoft-standard-WSL2
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: getrlimit(RLIMIT_NOFILE): 1048576:1048576
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: start worker processes
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: start worker process 29
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: start worker process 30
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: start worker process 31
frontend-1  | 2025/12/15 22:09:14 [notice] 1#1: start worker process 32
postgres-1  | sh: locale: not found
postgres-1  | 2025-12-15 22:09:14.567 UTC [35] WARNING:  no usable system locales were found
backend-1   | info: GymLogServer[0]
backend-1   |       Применяю миграции...
postgres-1  | performing post-bootstrap initialization ... ok
postgres-1  | initdb: warning: enabling "trust" authentication for local connections
postgres-1  | initdb: hint: You can change this by editing pg_hba.conf or using the option -A, or --auth-local and --auth-host, the next time you run initdb.
postgres-1  | syncing data to disk ... ok
postgres-1  |
backend-1   | fail: Microsoft.EntityFrameworkCore.Database.Connection[20004]
backend-1   |       An error occurred using the connection to database 'gymlogdb' on server 'tcp://postgres:5432'.
postgres-1  | Success. You can now start the database server using:
backend-1   | fail: GymLogServer[0]
postgres-1  |
backend-1   |       Ошибка при миграции!
postgres-1  |     pg_ctl -D /var/lib/postgresql/data -l logfile start
backend-1   |       Npgsql.NpgsqlException (0x80004005): Failed to connect to 172.20.0.2:5432
postgres-1  |
backend-1   |        ---> System.Net.Sockets.SocketException (111): Connection refused
postgres-1  | waiting for server to start....2025-12-15 22:09:15.902 UTC [41] LOG:  starting PostgreSQL 15.15 on x86_64-pc-linux-musl, compiled by gcc (Alpine 14.2.0) 14.2.0, 64-bit
backend-1   |          at Npgsql.Internal.NpgsqlConnector.Connect(NpgsqlTimeout timeout)
postgres-1  | 2025-12-15 22:09:15.912 UTC [41] LOG:  listening on Unix socket "/var/run/postgresql/.s.PGSQL.5432"
backend-1   |          at Npgsql.Internal.NpgsqlConnector.Connect(NpgsqlTimeout timeout)


postgres-1  | 2025-12-15 22:09:15.920 UTC [44] LOG:  database system was shut down at 2025-12-15 22:09:15 UTC
backend-1   |          at Npgsql.Internal.NpgsqlConnector.RawOpen(SslMode sslMode, NpgsqlTimeout timeout, Boolean async, CancellationToken cancellationToken)
backend-1   |          at Npgsql.Internal.NpgsqlConnector.<Open>g__OpenCore|214_1(NpgsqlConnector conn, SslMode sslMode, NpgsqlTimeout timeout, Boolean async, CancellationToken cancellationToken)
postgres-1  | 2025-12-15 22:09:15.936 UTC [41] LOG:  database system is ready to accept connections
postgres-1  |  done    at Npgsql.Internal.NpgsqlConnector.Open(NpgsqlTimeout timbackend-1   |          at Npgsql.PoolingDataSource.OpenNewConnector(NpgsqlConnection conn, NpgsqlTimeout timeout, Boolean async, CancellationToken cancellationToken)
postgres-1  | server started
backend-1   |          at Npgsql.PoolingDataSource.<Get>g__RentAsync|33_0(NpgsqlConnection conn, NpgsqlTimeout timeout, Boolean async, CancellationToken cancellationToken)
postgres-1  | CREATE DATABASE
postgres-1  |          at Npgsql.NpgsqlConnection.<Open>g__OpenAsync|42_0(Boolea



backend-1   |          at Npgsql.NpgsqlConnection.Open()h
postgres-1  |
backend-1   |          at Microsoft.EntityFrameworkCore.Storage.RelationalConnection.OpenDbConnection(Boolean errorsExpected)
backend-1   |          at Microsoft.EntityFrameworkCore.Storage.RelationalConnection.Open(Boolean errorsExpected)
postgres-1  | waiting for server to shut down....2025-12-15 22:09:16.183 UTC [41] LOG:  received fast shutdown request


backend-1   |          at Microsoft.EntityFrameworkCore.Storage.RelationalCommand.ExecuteReader(RelationalCommandParameterObject parameterObject)
postgres-1  | 2025-12-15 22:09:16.188 UTC [41] LOG:  aborting any active transactions


backend-1   |          at Microsoft.EntityFrameworkCore.Migrations.HistoryRepository.GetAppliedMigrations()47) exited with exit code 1
postgres-1  | 2025-12-15 22:09:16.195 UTC [42] LOG:  shutting down
backend-1   |          at Npgsql.EntityFrameworkCore.PostgreSQL.Migrations.Internal.NpgsqlHistoryRepository.GetAppliedMigrations()
postgres-1  | 2025-12-15 22:09:16.201 UTC [42] LOG:  checkpoint starting: shutdown immediate
backend-1   |          at Npgsql.EntityFrameworkCore.PostgreSQL.Migrations.Internal.NpgsqlMigrator.Migrate(String targetMigration)


postgres-1  | 2025-12-15 22:09:16.534 UTC [42] LOG:  checkpoint complete: wrote 921 buffers (5.6%); 0 WAL file(s) added, 0 removed, 0 recycled; write=0.082 s, sync=0.229 s, total=0.339 s; sync files=301, longest=0.012 s, average=0.001 s; distance=4239 kB, estimate=4239 kB
postgres-1  | 2025-12-15 22:09:16.549 UTC [41] LOG:  database system is shut down
postgres-1  |  done
postgres-1  | server stopped
postgres-1  |
postgres-1  | PostgreSQL init process complete; ready for start up.

sqlTimeout timeout, Boolean async, CancellationToken cancellationToken)
postgres-1  |
backend-1   |
                 at Npgsql.Internal.NpgsqlConnector.<Open>g__OpenCore|214_1(Npgs





2025-12-15 22:09:16.632 UTC [1] LOG:  listening on IPv6 address "::", port 5432







   at Microsoft.EntityFrameworkCore.Storage.RelationalConnection.OpenDbConnection(Boolean errorsExpected)
backend-1   |    at Microsoft.EntityFrameworkCore.Storage.RelationalConnection.OpenInternal(Boolean errorsExpected)
backend-1   |    at Microsoft.EntityFrameworkCore.Storage.RelationalConnection.Open(Boolean errorsExpected)
backend-1   |    at Microsoft.EntityFrameworkCore.Storage.RelationalCommand.ExecuteReader(RelationalCommandParameterObject parameterObject)
backend-1   |    at Microsoft.EntityFrameworkCore.Migrations.HistoryRepository.GetAppliedMigrations()
backend-1   |    at Npgsql.EntityFrameworkCore.PostgreSQL.Migrations.Internal.NpgsqlHistoryRepository.GetAppliedMigrations()
backend-1   |    at Npgsql.EntityFrameworkCore.PostgreSQL.Migrations.Internal.NpgsqlMigrator.Migrate(String targetMigration)
backend-1   |    at Microsoft.EntityFrameworkCore.RelationalDatabaseFacadeExtensions.Migrate(DatabaseFacade databaseFacade)
backend-1   |    at Program.<Main>$(String[] args) in /app/GymLogServer/Program.cs:line 72
backend-1 exited with code 139 (restarting)
backend-1   | info: GymLogServer[0]
backend-1   |       Применяю миграции...
postgres-1  | 2025-12-15 22:09:21.067 UTC [68] ERROR:  relation "__EFMigrationsHistory" does not exist at character 45
postgres-1  | 2025-12-15 22:09:21.067 UTC [68] STATEMENT:  SELECT "MigrationId", "ProductVersion"
backend-1   | fail: Microsoft.EntityFrameworkCore.Database.Command[20102]
backend-1   |       Failed executing DbCommand (24ms) [Parameters=[], CommandType='Text', CommandTimeout='30']
backend-1   |       SELECT "MigrationId", "ProductVersion"
backend-1   |       FROM "__EFMigrationsHistory"
backend-1   |       ORDER BY "MigrationId";
postgres-1  |   FROM "__EFMigrationsHistory"
postgres-1  |   ORDER BY "MigrationId"
backend-1   | info: Microsoft.EntityFrameworkCore.Migrations[20411]
backend-1   |       Acquiring an exclusive lock for migration application. See https://aka.ms/efcore-docs-migrations-lock for more information if this takes too long.
backend-1   | info: Microsoft.EntityFrameworkCore.Migrations[20402]
backend-1   |       Applying migration '20251006161057_Models2'.
backend-1   | info: Microsoft.EntityFrameworkCore.Migrations[20402]
backend-1   |       Applying migration '20251124112511_NewMigration'.
backend-1   | info: GymLogServer[0]
backend-1   |       Миграции применены!
backend-1   | warn: Microsoft.AspNetCore.DataProtection.Repositories.FileSystemXmlRepository[60]
backend-1   |       Storing keys in a directory '/root/.aspnet/DataProtection-Keys' that may not be persisted outside of the container. Protected data will be unavailable when container is destroyed. For more information go to https://aka.ms/aspnet/dataprotectionwarning
backend-1   | warn: Microsoft.AspNetCore.DataProtection.KeyManagement.XmlKeyManager[35]
backend-1   |       No XML encryptor configured. Key {14355545-cb04-4d8b-81a6-c6a8a8404bbb} may be persisted to storage in unencrypted form.
backend-1   | warn: Microsoft.AspNetCore.Hosting.Diagnostics[15]
backend-1   |       Overriding HTTP_PORTS '8080' and HTTPS_PORTS ''. Binding to values defined by URLS instead 'http://+:5198'.
backend-1   | info: Microsoft.Hosting.Lifetime[14]
backend-1   |       Now listening on: http://[::]:5198
backend-1   | info: Microsoft.Hosting.Lifetime[0]
backend-1   |       Application started. Press Ctrl+C to shut down.
backend-1   | info: Microsoft.Hosting.Lifetime[0]
backend-1   |       Hosting environment: Production
backend-1   | info: Microsoft.Hosting.Lifetime[0]
backend-1   |       Content root path: /app
frontend-1  | 172.20.0.1 - - [15/Dec/2025:22:09:37 +0000] "POST /api/user/login HTTP/1.1" 404 0 "http://localhost:3000/login" "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:146.0) Gecko/20100101 Firefox/146.0" "-"
frontend-1  | 172.20.0.1 - - [15/Dec/2025:22:10:01 +0000] "POST /api/user/register HTTP/1.1" 404 0 "http://localhost:3000/register" "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:146.0) Gecko/20100101 Firefox/146.0" "-"
frontend-1  | 172.20.0.1 - - [15/Dec/2025:22:10:07 +0000] "GET /register HTTP/1.1" 200 782 "-" "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:146.0) Gecko/20100101 Firefox/146.0" "-"
frontend-1  | 172.20.0.1 - - [15/Dec/2025:22:10:07 +0000] "GET /static/js/main.535d31dd.js HTTP/1.1" 200 285132 "http://localhost:3000/register" "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:146.0) Gecko/20100101 Firefox/146.0" "-"
frontend-1  | 172.20.0.1 - - [15/Dec/2025:22:10:42 +0000] "POST /api/user/login HTTP/1.1" 404 0 "http://localhost:3000/login" "Mozilla/5.0 (Windows NT 10.0; Winна в localStorage');
      return { success: false, error: 'Не авторизован' };
    }

    try {
      const res = await api.get('/trains');
      return { success: true, data: res.data || [] };
    } catch (err) {
      console.error('getTrains error', {
        status: err.response?.status,
        data: err.response?.data,
        message: err.message
      });
      // Явно различаем неавторизованность
      if (err.response?.status === 401) {
        return { success: false, error: 'Не авторизован' };
      }
      return { success: false, error: err.response?.data || err.message || 'Ошибка' };
    }
  },

  addTrain: async (trainData) => {
    const token = localStorage.getItem('token');
    if (!token) {
      return { success: false, error: 'Не авторизован' };
    }

    try {
      const res = await api.post('/trains/train', {
        type: trainData.type,
        description: trainData.description || '',
        duration: Number(trainData.duration),
        date: new Date(trainData.date).toISOString()
      });
      return { success: true, data: res.data };
    } catch (err) {
      console.error('addTrain error', { status: err.response?.status, data: err.response?.data, message: err.message });
      return { success: false, error: err.response?.data?.message || err.response?.data || 'Ошибка' };
    }
  },

  deleteTrain: async (userId, date) => {
    const token = localStorage.getItem('token');
    if (!token) return { success: false, error: 'Не авторизован' };
    try {
      const d = typeof date === 'string' ? date.split('T')[0] : date;
      const res = await api.delete(`/trains/delete/${d}`);
      return { success: true, data: res.data };
    } catch (err) {
      console.error('deleteTrain error', { status: err.response?.status, data: err.response?.data, message: err.message });
      return { success: false, error: err.response?.data || err.message };
    }
  }
};

export const TRAIN_TYPES = ['Тренажерный зал', 'Бег', 'Плавание', 'Йога', 'Фитнес', 'Боевое искусство'];
export const GYM_DESCRIPTIONS = ['Грудь', 'Спина', 'Ноги', 'Плечи', 'Руки', 'Пресс', 'Кардио'];
export const validateTrainData = (d) => {
  const e = {};
  if (!d.type) e.type = 'Выберите тип';
  if (!d.description?.trim()) e.description = 'Укажите описание';
  if (!d.duration || d.duration <= 0) e.duration = 'Укажите время';
  if (!d.date) e.date = 'Выберите дату';
  return { isValid: Object.keys(e).length === 0, errors: e };
};
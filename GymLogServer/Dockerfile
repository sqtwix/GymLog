# Dockerfile (backend)
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /app

# Чистим кэш NuGet — это убивает NETSDK1064 навсегда
RUN dotnet nuget locals all --clear

# Копируем только нужный csproj
COPY GymLogServer/*.csproj ./GymLogServer/

# Восстанавливаем
RUN dotnet restore ./GymLogServer/GymLogServer.csproj

# Копируем всё остальное
COPY . .

# --- changed: убедиться, что зависимости восстановлены перед publish ---
RUN dotnet restore ./GymLogServer/GymLogServer.csproj
RUN dotnet publish ./GymLogServer/GymLogServer.csproj -c Release -o out

# Runtime
FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app
COPY --from=build /app/out ./

EXPOSE 5198
ENV ASPNETCORE_URLS=http://+:5198

ENTRYPOINT ["dotnet", "GymLogServer.dll"]
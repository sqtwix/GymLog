# Stage 1 — build
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /app

COPY . .
RUN dotnet restore
RUN dotnet publish -c Release -o out

# Stage 2 — runtime  
FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app

COPY --from=build /app/out .

ENV ASPNETCORE_HTTP_PORTS=5198
EXPOSE 5198

ENTRYPOINT ["dotnet", "GymLogServer.dll"]
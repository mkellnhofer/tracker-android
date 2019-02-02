./gradlew clean
./gradlew assembleProductionRelease

jarsigner -verbose \
  -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore ~/keys/play-store.jks \
  -storepass:file ~/keys/play-store.key \
  -signedjar app/build/outputs/apk/production/release/app-production-release-signed.apk \
  app/build/outputs/apk/production/release/app-production-release-unsigned.apk \
  signing_key

zipalign -v 4 \
  app/build/outputs/apk/production/release/app-production-release-signed.apk \
  app/build/outputs/apk/production/release/app-production-release.apk

cp app/build/outputs/apk/production/release/app-production-release.apk tracker.apk
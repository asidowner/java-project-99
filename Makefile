lint:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

generate_private_key:
	openssl genpkey -out src/main/resources/certs/private.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048

generate_public_key:
	openssl rsa -in src/main/resources/certs/private.pem -pubout -out src/main/resources/certs/public.pem

generate_rsa: generate_private_key generate_public_key
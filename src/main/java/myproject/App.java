package myproject;

import com.pulumi.Pulumi;
import com.pulumi.aws.iam.Role;
import com.pulumi.aws.iam.RoleArgs;
import com.pulumi.aws.iam.inputs.PolicyDocumentArgs;
import com.pulumi.aws.rolesanywhere.ProfileArgs;
import com.pulumi.aws.rolesanywhere.TrustAnchorArgs;
import com.pulumi.aws.rolesanywhere.inputs.TrustAnchorSourceArgs;
import com.pulumi.aws.rolesanywhere.inputs.TrustAnchorSourceSourceDataArgs;
import com.pulumi.core.Output;
import com.pulumi.aws.s3.Bucket;
import com.pulumi.aws.rolesanywhere.TrustAnchor;
import com.pulumi.aws.rolesanywhere.Profile;
import com.pulumi.resources.ResourceTransformation;
import com.pulumi.resources.StackOptions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {

        var rootPem = getRootPem();
        var intermediatePem = getIntermediatePem();
        System.out.println(rootPem);
        System.out.println(intermediatePem);
        /*Pulumi.run(ctx-> {
            ctx.export("root", rootPem);
            ctx.export("intermediate", rootPem);
        });*/

        Pulumi.run(ctx -> {
            var trustAnchor = new TrustAnchor("mac-mini-ca-trust-anchor", TrustAnchorArgs.builder().source(TrustAnchorSourceArgs.builder()
                    .sourceData(TrustAnchorSourceSourceDataArgs.builder()
                            .x509CertificateData(rootPem + intermediatePem)
                            .build())
                    .sourceType("CERTIFICATE_BUNDLE")
                    .build())
                    .name("Mac Mini CA TA")
                    .enabled(true)
                    .build());

            var role = new Role("DEMO_S3_READ_ONLY",
                    RoleArgs.builder()
                            .name("DEMO_S3_READ_ONLY")
                            .assumeRolePolicy(
                                PolicyDocumentArgs.builder()
                                        .
                                        .build()
                            )
                            .build()
            );

            var profile = new Profile("mac-mini-ca-profile", ProfileArgs.builder()
                    .name("Mac Mini CA Profile")
                    .build()
            );

            ctx.export("trustAnchorId", trustAnchor.id());
            ctx.export("trustAnchorArn", trustAnchor.arn());
        });
    }

    private static String getRootPem() throws IOException, InterruptedException {
        return getContentFromUrl("https://ca-mini.01101011.in/roots.pem");
    }

    private static String getIntermediatePem() throws IOException, InterruptedException {
        return getContentFromUrl("https://ca-mini.01101011.in/intermediates.pem");
    }

    private static String getContentFromUrl(String url) throws IOException, InterruptedException {
        return HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString())
                .body();
    }
}

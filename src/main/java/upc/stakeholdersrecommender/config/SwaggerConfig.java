package upc.stakeholdersrecommender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(regex("/upc/stakeholders-recommender.*"))
                .paths(PathSelectors.any())
                .build().apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Stakeholder Recommender",
                "The goal of the Stakeholder Recommender component is to recommend stakeholders that can contribute to a specific requirement. For making such " +
                        "recommendation, the component matches the keywords of the requirements and the skills of the persons. In addition, the recommendation also takes " +
                        "into account the availability of users. The three items needed to make the recommendations are learnt by the component as follows:\n" +
                        "\n" +
                        "1.  Keywords of the requirements. These keywords represent the main skills a person should have to cover this requirement (by covering a requirement" +
                        " we mean the contribution in a requirement or even its implementation). For extracting them, the title and description of the requirements enter in a" +
                        " natural language processing pipeline, which output is the keywords of the requirements.\n" +
                        "\n" +
                        "2.  Skills of the persons. The skills represent abilities that the persons have achieved by contributing to requirements in the past. For computing " +
                        "these skills, the component needs to know for each person, the requirements s/he has contributed to. The skills of a person are the keywords of the " +
                        "requirements of which the person is responsible for. Additionally, for each skill a user has, the component computes a proficiency value (i.e., how expert " +
                        "the user is in this task). Finally, this proficiency value is deprecated by  a factor time. As an example, imagine persons A and B are responsible " +
                        "for the same  number of requirements that need a specific skill. However, the last time person A   worked on such requirements is already one year ago, " +
                        "while person B has been  working recently on these requirements. It makes sense that the proficiency of B skill is higher than the proficiency of A, " +
                        "since B has the knowledge closer in time. The time factor tries to balance these situations by deprecating the proficiency taking into " +
                        "account the date of the requirements. In addition, the skills of users are also gathered  from the implicit feedback got from an external OpenReq component" +
                        " ri-logging. With this implicit information, the component learns new skills or updates the proficiency of the skills of the persons by using the information" +
                        " of how users interact with a user interface to browse, create and edit requirements.\n" +
                        "\n" +
                        "3.  Availability of persons. Persons have a number of hours to work on a project. The component calculates how many hours the persons have still available for" +
                        " a project. After that, the available hours are normalized in a rank from 0 to 1. The use of implicit context information and availability is parameterized, so " +
                        "users can choose when to use it and when not. \n",
                "2.1.1",
                null,
                null,
                null,
                null);
    }

}

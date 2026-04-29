package com.hopeful117.cv_analyzer.extractor;

import com.hopeful117.cv_analyzer.model.SkillRequirement;
import org.springframework.stereotype.Service;

@Service
public class JobImportanceDetector implements ImportanceDetector {

    @Override
    public SkillRequirement detect(String keyword, String jobOfferText) {
    String lower = jobOfferText.toLowerCase();
    String lowerKeyword = keyword.toLowerCase();
        if(matchesSignal(
                lower,
                lowerKeyword,
                ImportanceSignals.MANDATORY_SIGNALS)){

            return new SkillRequirement(
                    keyword,
                    30,
                    true
            );
        }


        if(matchesSignal(
                lower,
                lowerKeyword,
                ImportanceSignals.OPTIONAL_SIGNALS)){

            return new SkillRequirement(
                    keyword,
                    10,
                    false
            );
        }



        return new SkillRequirement(
                keyword,
                20,
                false
        );
    }



    private boolean matchesSignal(
            String text,

            String keyword,

            Iterable<String> signals){

        String[] words =
                text.split("\\s+");

        for(int i=0; i<words.length; i++){

            if(words[i].equals(keyword)){

                int start =
                        Math.max(0, i-5);

                int end =
                        Math.min(
                                words.length,
                                i+6
                        );


                StringBuilder context =
                        new StringBuilder();

                for(int j=start; j<end; j++){

                    context.append(
                            words[j]
                    ).append(" ");
                }

                String window =
                        context.toString();


                for(String signal : signals){

                    if(window.contains(signal)){

                        return true;
                    }
                }
            }
        }

        return false;



    }
    }



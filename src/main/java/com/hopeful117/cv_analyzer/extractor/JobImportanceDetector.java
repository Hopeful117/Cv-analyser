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

        for(String signal : signals){

            if(
                    text.contains(
                            signal + " " + keyword
                    )

                            ||

                            text.contains(
                                    keyword + " " + signal
                            )
            ){
                return true;
            }
        }

        return false;
    }
    }



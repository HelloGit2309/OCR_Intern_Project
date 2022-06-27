target_link_libraries( # Specifies the target library.
                   native-lib

                   -lgomp

                   # Links the target library to the log library
                   # included in the NDK.
                   ${log-lib} )
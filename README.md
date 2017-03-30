This is the Annotated Java Call Graph (JCG) project. The JARs it produces are supposed to be used as test input for call graph bilders. 

The project contains several sub-projects each implementing a library handling arithmetic expressions and possibly an application using the library. Each sub-project features a certain kind of cases relevant to construction of java call graphs. The relevant call graph edges and all entry points are annotated with special annotations to allow automated construction of ground truth.

FROM openjdk:21-slim-bullseye
LABEL authors="nicokunz"

# Install required tools (npm, pip, git)
RUN apt-get update && apt-get install -y \
    npm \
    python3-pip \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Setup python link
RUN ln -s /usr/bin/python3 /usr/local/bin/python3

# setup more recent version of node needed for jelly
RUN npm install -g n
RUN n 22.11.0
RUN pip install --upgrade setuptools==58.0.4 wheel

# Install call graph generators
RUN npm install -g @persper/js-callgraph@1.3.2
RUN npm install -g @cs-au-dk/jelly@0.10.0
RUN pip install PyCG==0.0.7
RUN pip3 install code2flow==2.5.1
RUN npm install -g acorn@8.14.0 # code2flow needs acorn for js parsing
RUN pip3 install pyan3==1.1.1 # version 1.2.0 crashes

# Install TAJS
RUN curl -sL https://www.brics.dk/TAJS/dist/tajs-all.jar -o /usr/local/bin/tajs-all.jar

# Install Jarvis
RUN git clone https://github.com/nico-kunz/pythonJaRvis.github.io.git /usr/local/bin/jarvis

# Create working directory and copy application files
WORKDIR /app
COPY . /app

RUN echo "tajs = /usr/local/bin/tajs-all.jar" >> /app/tajs.properties
RUN echo "jarvis = /usr/local/bin/jarvis/Jarvis/tool/Jarvis/jarvis_cli.py" >> /app/adapters.properties
RUN touch /app/package.json # Jelly needs a package.json file to detect base directory

# Set up Scala
#RUN curl -sL https://github.com/sbt/sbt/releases/download/v1.8.0/sbt-1.8.0.tgz | tar xz -C /usr/local && \
#    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt

# Install Coursier
# Install Coursier
RUN curl -fLo cs-x86_64-pc-linux.gz https://github.com/coursier/coursier/releases/download/v2.1.16/cs-x86_64-pc-linux.gz && \
    gunzip cs-x86_64-pc-linux.gz && \
    chmod +x cs-x86_64-pc-linux && \
    mv cs-x86_64-pc-linux /usr/local/bin/coursier

RUN coursier install scala-cli && \
    ln -s ~/.local/share/coursier/bin/scala-cli /usr/local/bin/scala-cli

RUN coursier install sbt && \
    ln -s ~/.local/share/coursier/bin/sbt /usr/local/bin/sbt

# Add coursier binaries to PATH
ENV PATH="${PATH}:/root/.local/share/coursier/bin"

#RUN sbt clean compile
RUN chmod +x /app/run_eval.sh


CMD ["/app/run_eval.sh"]

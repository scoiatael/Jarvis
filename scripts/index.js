const DownloadLink = () => {
  return (
    <a href="#" className="btn btn-lg btn-default">Download</a>
  );
}

const Page = () => {
  return (
    <div className="inner cover">
      <h1 className="cover-heading">Visual Programming Editor - reimagined.</h1>
      <p className="lead">Jarvis is a simple visual Clojure editor.</p>
      <p className="lead">
        <DownloadLink />
      </p>
    </div>
    );
}

ReactDOM.render(
  <Page />,
  document.getElementById('content')
);
